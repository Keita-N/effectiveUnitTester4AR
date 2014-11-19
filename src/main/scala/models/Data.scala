package models

/**
 * 分析レポート単体試験用 テストデータモデル
 * Created by k.yanagida on 2014/06/26.
 */
case class TestData(tableName: String, dirName: String, fileName: String, dt: Option[String]) {

  val ddl4AmpSysDB =
    """
      |delete from amp_sys_db.$table_name$;
      |load data local infile 'C:/Users/k.yanagida/usr/misc/out/AMP_SYS_DB/$file_name$' into table amp_sys_db.$table_name$;
    """.stripMargin
  val ddl4Hive =
    """
      |alter table $table_name$ drop partition (dt='$dt$');
      |load data local inpath '/home/k.yanagida/data/out/HIVE/$file_name$' overwrite into table $table_name$ partition(dt='$dt$');
    """.stripMargin

  /**
   * DDLを生成します。
   * @return DDL
   */
  def generateDDL(): String = {
    val ddl = dt match {
      case Some(x) => ddl4Hive.replace("$dt$", x)
      case None => ddl4AmpSysDB
    }
    ddl.replace("$table_name$", tableName).replace("$file_name$", fileName)
  }
}

/**
 * 期待値データモデル
 * Created by k.yanagida on 2014/06/26.
 */
case class ExpectedData(
                         dt: String,
                         framePosition: String,
                         siteType: String,
                         tagType: String,
                         conditions: List[(String, String)],
                         var price: Int,
                         var bidPriceGross: Int,
                         var imp: Int,
                         var click: Int,
                         var cv: Int,
                         var directCV: Int,
                         var vtCV: Int) {

  /**
   * インプレッション数を加算します。
   * @param arg インプレッション数
   */
  def addImp(arg: Int) = {
    imp += arg
  }

  /**
   * クリック数を加算します。
   * @param arg クリック数
   */
  def addClick(arg: Int) = {
    click += arg
  }

  /**
   * 金額を加算します。
   * @param arg 金額
   */
  def addPrice(arg: Int) = {
    price += arg
  }

  /**
   * 入札金額（グロス）を加算します。
   * @param arg 入札金額（グロス）
   */
  def addBidPriceGross(arg: Int) = {
    bidPriceGross += arg
  }

  /**
   * CV数を加算します。
   * @param arg CV数
   */
  def addCV(arg: Int) = {
    cv += arg
  }

  /**
   * 直接CV数を加算します。
   * @param arg 直接CV数
   */
  def addDirectCV(arg: Int) = {
    directCV += arg
  }

  /**
   * VTCV数を加算します。
   * @param arg VTCV数
   */
  def addVtCV(arg: Int) = {
    vtCV += arg
  }

  /**
   * CTRを算出します。
   * @return CTR
   */
  def calculateCTR(): BigDecimal = {
    imp match {
      case 0 => 0
      case _ => ((BigDecimal(click) / BigDecimal(imp)) * 100).setScale(5, BigDecimal.RoundingMode.HALF_UP)
    }
  }

  /**
   * 消化予算を算出します。
   * @return 消化予算
   */
  def calculateBudgetUsed(): BigDecimal = {
    (BigDecimal(bidPriceGross) / BigDecimal(1000000) + price).setScale(3, BigDecimal.RoundingMode.HALF_UP)
  }

  /**
   * ポストクリックCV数を算出します。
   * @return ポストクリックCV数
   */
  def calculatePostClickCV(): Int = {
    cv - directCV
  }

  /**
   * 枠位置の名称を取得します。
   * @return 枠位置の名称
   */
  def getFramePositionName(): String = {
    framePosition match {
      case "0" => "ファーストビュー以外"
      case "1" => "ファーストビュー"
      case "9" => "不明"
      case _ => ""
    }
  }

  /**
   * サイト種別の名称を取得します。
   * @return サイト種別の名称
   */
  def getSiteTypeName(): String = {
    siteType match {
      case "0" => "PC"
      case "1" => "最適化サイト"
      case "2" => "アプリ枠"
      case _ => ""
    }
  }

  /**
   * タグ種別の名称を取得します。
   * @return タグ種別の名称
   */
  def getTagTypeName(): String = {
    tagType match {
      case "0" => "PC"
      case "1" => "インライン"
      case "2" => "オーバーレイ"
      case "9" => "インライン/オーバレイ"
      case _ => ""
    }
  }
}