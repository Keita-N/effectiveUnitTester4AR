package configurations

import com.yammer.dropwizard.config.Configuration
import org.hibernate.validator.constraints.NotEmpty

/**
 * Created by k.yanagida on 2014/06/19.
 */
class AnalysisReportConfiguration extends Configuration {

  @NotEmpty
  val hiveBookName: String = "Hive_Data.xlsx"

  @NotEmpty
  val ampSysDbBookName: String = "AMP_SYS_DB_Data.xlsx"

  @NotEmpty
  val tmpDir: String = "tmp/"

  @NotEmpty
  val archiveDir: String = "archive/"

}
