import Configurations.ConfigKeyException
import Simulations.{DataCenterSimulations, DynamicVmScaling, IaaSSimulation, SaaSSimulation}
import com.typesafe.config.{Config, ConfigException}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.scalatest.funsuite.AnyFunSuite
import Configurations.Myconfig

class SimulationTests extends AnyFunSuite{

  test("Cloudlet Count should be more than one for Dynamic VM Scaling"){
    assert(DynamicVmScaling.simulate()>1)
  }

test("Passing Invalid Config Key should through ConfigKeyException") {
  assertThrows[ConfigKeyException] {
//    no Service with "NoService" nmae exits in Configuration file
    SaaSSimulation.simulate(serviceName = "NoService")
  }
}

test("Vms should be assigned to the host present in same location")
  {
    val broker = IaaSSimulation.dataCenterLocationSelectivitySimulate(true)
    assert(broker.getCloudletFinishedList.get(0).asInstanceOf[Cloudlet].getVm.getTimeZone.equals(broker.getCloudletFinishedList.get(0).asInstanceOf[Cloudlet].getVm.getHost.getDatacenter.getTimeZone))
  }

  test("Prices of tasks execution should increase if everything remains same just the data center prices are changed")
  {
    assert(DataCenterSimulations.simulate(dataCenterCharges = "dataCenterChargesMaximals")>DataCenterSimulations.simulate(dataCenterCharges = "dataCenterChargesMinimals"))
  }

  test("Specific configuration cloudlet should be created for specific service")
  {
    val generatedCloudlet = SaaSSimulation.simulate(serviceName = "service2").getCloudletFinishedList.get(0).asInstanceOf[Cloudlet]

    val actualCloudlet:Config = Myconfig.getObject(Myconfig.getObject("service2").getStringList("cloudlets").get(0))

    assert( actualCloudlet.getDouble("length").equals(generatedCloudlet.getLength.asInstanceOf[Double]))
    assert (actualCloudlet.getDouble("pesNumber").equals(generatedCloudlet.getNumberOfPes.asInstanceOf[Double]))
    assert(actualCloudlet.getDouble("fileSize").equals(generatedCloudlet.getFileSize.asInstanceOf[Double]))
    assert(actualCloudlet.getDouble("outFileSize").equals(generatedCloudlet.getOutputSize.asInstanceOf[Double]))
  }

}
