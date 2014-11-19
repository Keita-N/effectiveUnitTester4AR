package utils

import java.util

import org.apache.commons.lang.math.NumberUtils
import org.bbreak.excella.core.tag.TagParser
import org.bbreak.excella.core.tag.excel2java.{MapsParser, ArraysParser, MapParser, ListParser}
import org.bbreak.excella.core.{BookController, SheetData}

/**
 * Created by k.yanagida on 2014/11/17.
 */
object GenericUtility {

  /**
   * Excelファイルからシートデータを取得します。
   * @param resource ファイル名
   * @return シートデータ
   */
  def getSheetDatas(resource: String): util.Collection[SheetData] = {
    val controller = new BookController(resource)
    controller.addTagParser(getTagParser("@Maps").get)
    controller.parseBook
    controller.getBookData.getSheetDatas
  }

  /**
   * TagParser（Excelデータ解析用）を取得します。
   * @param tag タグ文字列
   * @return TagParser
   */
  def getTagParser(tag: String): Option[TagParser[_]] = {
    tag match {
      case "@List" => Some(new ListParser(tag))
      case "@Map" => Some(new MapParser(tag))
      case "@Arrays" => Some(new ArraysParser(tag))
      case "@Maps" => Some(new MapsParser(tag))
      case _ => None
    }
  }

  /**
   * 引数をString型に変換して返却します。
   * @param value 変換する値
   * @return String型の値
   */
  def convertValueToString(value: Object): String = {
    Option(value).getOrElse("").toString match {
      case x if (NumberUtils.isNumber(x)) => String.valueOf(java.lang.Double.valueOf(x).intValue())
      case x => x
    }
  }

  /**
   * 引数をInt型に変換して返却します。
   * @param value 変換する値
   * @return Int型の値
   */
  def convertValueToInt(value: Object): Int = {
    Option(value).getOrElse("").toString match {
      case x if (NumberUtils.isNumber(x)) => java.lang.Double.valueOf(x).intValue()
      case _ => 0
    }
  }


}
