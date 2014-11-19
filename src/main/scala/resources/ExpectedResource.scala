package resources

import java.net.URLDecoder
import java.util
import javax.ws.rs.{GET, Path, Produces, QueryParam}
import com.yammer.metrics.annotation.Timed
import models.ExpectedData
import org.bbreak.excella.core.SheetData
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import utils.GenericUtility
import scala.collection.JavaConversions._
import models.ExpectedData

/**
 * Created by k.yanagida on 2014/11/17.
 */
@Path("/expected")
@Produces(Array("application/json; charset=utf-8"))
class ExpectedResource(val hiveBookName: String) {

  @GET
  @Timed
  def calculate(@QueryParam("tableName") tableName: String,
                @QueryParam("dt") dt: String,
                @QueryParam("framePosition") framePosition: String,
                @QueryParam("siteType") siteType: String,
                @QueryParam("tagType") tagType: String,
                @QueryParam("conditions") conditions: String): String = {

    val conditionList = URLDecoder.decode(conditions, "utf-8").split("&").map { condition =>
      println(condition)
      val set = condition.split("=")
      (set(0), set(1))
    }.toList

    // 期待値データを生成
    val expectedData = generateExpectedData(tableName, dt, framePosition, siteType, tagType, conditionList)

    pretty(render(("expected" ->
      ("frame_position" -> expectedData.getFramePositionName) ~
        ("site_type" -> expectedData.getSiteTypeName) ~
        ("tag_type" -> expectedData.getTagTypeName) ~
        ("used_budget" -> expectedData.calculateBudgetUsed) ~
        ("impression" -> expectedData.imp) ~
        ("click" -> expectedData.click) ~
        ("ctr" -> expectedData.calculateCTR) ~
        ("conversion" -> expectedData.cv) ~
        ("post_click_cv" -> expectedData.calculatePostClickCV) ~
        ("direct_cv" -> expectedData.directCV) ~
        ("vtcv" -> expectedData.vtCV))))
  }

  /**
   * 期待値データを生成します。
   * @param tableName テーブル名称
   * @param dt dt（Hiveのパーティション）
   * @param framePosition 掲載位置
   * @param siteType サイト種別
   * @param tagType タグ種別
   * @param conditions 検索条件
   * @return 期待値データ
   */
  def generateExpectedData(tableName: String,
                           dt: String,
                           framePosition: String,
                           siteType: String,
                           tagType: String,
                           conditions: List[(String, String)]) = {
    // Hiveのシートデータを取得
    val sheetDatas = GenericUtility.getSheetDatas(getClass.getClassLoader.getResource(hiveBookName).getFile)
    // 期待値データを初期化
    val expectedData = ExpectedData(dt, framePosition, siteType, tagType, conditions, 0, 0, 0, 0, 0, 0, 0)
    // 期待値を集計
    aggregateExpectedData(sheetDatas.filter { sheetData =>
      (sheetData.getSheetName.startsWith(tableName)) ||
        (sheetData.getSheetName.startsWith("middle_cv_sum_daily"))
    }, expectedData)
  }

  /**
   * 期待値データを集計します。
   * @param sheetDatas Excelファイルのシートデータ
   * @param expectedData 集計結果格納用
   * @return 期待値データ
   */
  def aggregateExpectedData(sheetDatas: util.Collection[SheetData], expectedData: ExpectedData): ExpectedData = {
    // シートの数分繰り返し
    sheetDatas.foreach { sheetData =>
      // @Mapsタグでデータを取得
      sheetData.get("@Maps").asInstanceOf[util.List[util.Map[String, Object]]].filter {
        /*
        指定した条件でフィルタ
         */
        // パーティション(dt)
        dataMap => dataMap.get("dt").toString.startsWith(expectedData.dt) &&
          // 枠位置
          GenericUtility.convertValueToString(dataMap.get("frame_position")).equals(expectedData.framePosition) &&
          // サイト種別
          GenericUtility.convertValueToString(dataMap.get("site_type")).equals(expectedData.siteType) &&
          // タグ種別
          GenericUtility.convertValueToString(dataMap.get("tag_type")).equals(expectedData.tagType) &&
          // 検索条件
          expectedData.conditions.forall { condition =>
            val key = condition._1 match {
              /*
              必要に応じてkeyを変換
               */
              // ページカテゴリ
              case n if (n.equals("page_category") && sheetData.getSheetName.startsWith("middle_content_genre")) => "category_id"
              // ADJUSTセグメント
              case n if (n.equals("adjust_segment") && sheetData.getSheetName.startsWith("middle_adjust_segment_genre")) => "attribute_id"
              // デモグラフィック
              case n if (n.equals("demographic") && sheetData.getSheetName.startsWith("middle_demographic_genre")) => "attribute_id"
              // ライフスタイル
              case n if (n.equals("lifestyle") && sheetData.getSheetName.startsWith("middle_lifestyle_genre")) => "attribute_id"
              case n => n
            }
            // 指定した条件の値に合致するものを返す
            GenericUtility.convertValueToString(dataMap.get(key)).equals(condition._2)
          }
        // 抽出したリストを繰り返し
      } foreach { dataMap =>
        dataMap.foreach { data =>
          // 期待値データを加算
          addExpectedData(expectedData, data._1, GenericUtility.convertValueToInt(data._2))
        }
      }
    }
    // 期待値データを返却
    expectedData
  }

  /**
   * 期待値データを加算します。
   * @param data 加算対象の期待値データ
   * @param key 加算項目のキー
   * @param value 加算する値
   * @return 期待値データ
   */
  def addExpectedData(data: ExpectedData, key: String, value: Int): ExpectedData = {
    key match {
      // インプレッション数
      case "impression" => data.addImp(value)
      // クリック数
      case "click" => data.addClick(value)
      // 金額
      case "price" => data.addPrice(value)
      // 入札単価（グロス）
      case "bid_price_gross" => data.addBidPriceGross(value)
      // CV数
      case "cv_cnt" => data.addCV(value)
      // 直接CV数
      case "direct_cv_cnt" => data.addDirectCV(value)
      // VTCV数
      case "vtcv_cnt" => data.addVtCV(value)
      case _ =>
    }
    data
  }
}
