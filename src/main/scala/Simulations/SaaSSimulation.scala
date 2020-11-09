package Simulations

import java.util

import Configurations.Myconfig
import Simulations.Helper.{logger, _}
import com.typesafe.config.Config
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerBestFit}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudbus.cloudsim.vms.Vm
import org.slf4j.{Logger, LoggerFactory}

object SaaSSimulation {

  val logger: Logger = LoggerFactory.getLogger(SaaSSimulation.getClass)
  /**
   * To simulate the SaaS service creating scenario depending on type of service and place where to
   * create it
   *
   * @param serviceName name of service whose instance you wanted to create
   * @param place       location where its Vm Should be created
   */

  def simulate(serviceName: String = "service1", place: String = "Mumbai"):DatacenterBroker = {
    logger.info("Simulation for SaaS started....")
    val cloudsim: CloudSim = new CloudSim()
    /*Creates a Broker that will act on behalf of a cloud user (customer).*/
    val broker0: DatacenterBroker = new DatacenterBrokerBestFit(cloudsim)
    val serviceConfig: Config = Myconfig.getObject(serviceName)

    val dc: Datacenter = createDataCenter(cloudsim, serviceConfig.getString("dataCenterSpec"), serviceConfig.getString("dataCenterCharges"), serviceConfig.getString("dataCenterCapacity"), new VmAllocationPolicyBestFit, new VmSchedulerTimeShared(), place)

    broker0.submitCloudletList(createServiceCloudlets(serviceName))
    broker0.submitVmList(createServiceVms(serviceName, place))

    cloudsim.start()
    printSummary(broker0)
    broker0
  }

  /** +
   * creates Clodlets for this provided serviceName depending on config maintained for this service
   *
   * @param serviceName service name whose instance you want to create
   * @return new Cloudlet for this service instance.
   */
  def createServiceCloudlets(serviceName: String): util.List[Cloudlet] = {
    val clNames: util.List[String] = Myconfig.getObject(serviceName).getStringList("cloudlets")
    coreCreateCloudLetList(clNames, new UtilizationModelFull)
  }

  //  creates Vms for this provided serviceName depending on config maintained for this service
  /** +
   *
   * @param serviceName name service whose instance you want to create
   * @param place       where you want your instance service to be created
   * @return new Vm need for running this service instance
   */
  def createServiceVms(serviceName: String, place: String): util.List[Vm] = {
    val vmNames: util.List[String] = Myconfig.getObject(serviceName).getStringList("vms")
    coreCreateVmList(vmNames, new CloudletSchedulerTimeShared, place)
  }
}
