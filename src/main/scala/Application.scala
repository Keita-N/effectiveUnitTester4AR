import com.yammer.dropwizard.ScalaService
import com.yammer.dropwizard.bundles.ScalaBundle
import com.yammer.dropwizard.config.{Bootstrap, Environment}
import configurations.AnalysisReportConfiguration
import resources.{ExpectedResource, TestResource}

/**
 * Created by k.yanagida on 2014/06/19.
 */
object Application extends ScalaService[AnalysisReportConfiguration] {

  def initialize(bootstrap: Bootstrap[AnalysisReportConfiguration]) {
    bootstrap.setName("example")
    bootstrap.addBundle(new ScalaBundle)
  }

  def run(configuration: AnalysisReportConfiguration, environment: Environment) {
    environment.addResource(new TestResource(configuration.hiveBookName, configuration.ampSysDbBookName, configuration.tmpDir, configuration.archiveDir))
    environment.addResource(new ExpectedResource(configuration.hiveBookName))
  }

}

