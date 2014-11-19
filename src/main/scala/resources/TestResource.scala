package resources

import java.io._
import java.util
import javax.ws.rs._

import com.yammer.metrics.annotation.Timed
import models.TestData
import utils.{GenericUtility, ZipUtility}

import scala.collection.JavaConversions._

/**
 * Created by k.yanagida on 2014/06/19.
 */
@Path("/test")
@Produces(Array("application/x-zip-compressed;"))
class TestResource(val hiveBookName: String, val ampSysDbBookName: String, val tmpDir: String, val archiveDir: String) {

  /**
   * テストデータを生成します。
   */
  @GET
  @Timed
  def generate(): Object = {

    // 一時ディレクトリ配下のファイルを削除
    tools.nsc.io.Directory(tmpDir).deepFiles.foreach { f => f.delete()}

    // Excelブックリスト
    val books = List(
      GenericUtility.getSheetDatas(getClass.getClassLoader.getResource(hiveBookName).getFile),
      GenericUtility.getSheetDatas(getClass.getClassLoader.getResource(ampSysDbBookName).getFile))
    // DDL出力用
    val ddlOut = getPrintWriter(tmpDir, "example.ddl")
    // ブックの数分繰り返し
    books.foreach { sheetDatas =>
      // DDL格納用
      val ddl = new StringBuilder
      // シートの数分繰り返し
      sheetDatas.foreach { sheetData =>
        // @Mapsタグでデータを取得
        val dataList = sheetData.get("@Maps").asInstanceOf[util.List[util.Map[String, Object]]]
        // 出力データの生成
        val outputData = sheetData.getSheetName match {
          // シート名に"$"を含む場合
          case n if n.indexOf("$") != -1 => {
            // テーブル名の取得（"$"より前）
            val tableName = n.substring(0, n.lastIndexOf("$"))
            // パーティション（dt）の取得
            val dt = dataList.get(0).get("dt").toString
            // テストデータの設定
            TestData(tableName, tmpDir + "Hive/", tableName + ".tsv." + dt, Option(dt))
          }
          // 上記以外の場合
          case n => {
            // テストデータの設定
            TestData(n, (tmpDir + "AMP_SYS_DB/"), (n + ".tsv"), None)
          }
        }
        // DDLの作成
        ddl.append(outputData.generateDDL())
        // データ出力用
        val dataOut = getPrintWriter(outputData.dirName, outputData.fileName)
        // レコード数分繰り返し
        dataList.foreach { dataMap =>
          // データ格納用
          val str = new StringBuilder
          // カラム数分繰り返し（key=カラム名, value=値）
          dataMap.foreach { data =>
            data._1 match {
              // パーティション（dt）の場合は何もしない
              case "dt" =>
              // 上記以外はタブ区切りで値を格納
              case _ => {
                str.append(GenericUtility.convertValueToString(data._2)).append("\t")
              }
            }
          }
          // 末尾のタブ文字を削除
          str.deleteCharAt(str.lastIndexOf("\t"))
          // データを出力
          dataOut.println(str)
        }
        // データ出力用オブジェクトの終了
        dataOut.close()
      }
      // DDLを出力
      ddlOut.println(ddl)
    }
    // DDL出力用オブジェクトの終了
    ddlOut.close()
    // テストデータを圧縮
    val zipFile = ZipUtility.compressDirectory((tmpDir + System.currentTimeMillis() + ".zip"), tmpDir)
    println(zipFile.getAbsolutePath)
    zipFile
  }

  /**
   * PrintWriter（ファイル書き込み用）を取得します。
   * @param dirName 出力するディレクトリ名
   * @param fileName 出力するファイル名
   * @return PrintWriter
   */
  def getPrintWriter(dirName: String, fileName: String): PrintWriter = {
    val dir = new File(dirName)
    if (!dir.exists()) dir.mkdirs()
    new PrintWriter((if (dirName.endsWith("/")) dirName else dirName + "/") + fileName)
  }

}