package Simulations

import java.util

import Configurations.DataCenterBrokerEnum
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicy, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerBestFit, DatacenterBrokerFirstFit, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.schedulers.vm.{VmScheduler, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.vms.Vm
import org.slf4j.{Logger, LoggerFactory}

/**
 * Class used for simulating the scenarios which are in control of cloud providers
 * like broker type, prices, capacity of datacenter in host terms
 */
object DataCenterSimulations {

  val logger: Logger = LoggerFactory.getLogger(DataCenterSimulations.getClass)
  /**
   *
   * @param dataCenterBrokerEnum broker to be used
   * @param dataCenterSpec       config specifying the technical specification of data center
   * @param dataCenterCharges    config specifying the charge of datacenter
   * @param dataCenterCapacity   config specifying the capacity of datacenter
   * @param vmAllocationPolicy   policy host will follow for Vm allocation
   * @param vmScheduler          scheduler for Vm
   */
  def simulate(dataCenterBrokerEnum: DataCenterBrokerEnum.Value = DataCenterBrokerEnum.DatacenterBrokerSimple, dataCenterSpec: String = "dataCenter1", dataCenterCharges: String = "dataCenterChargesMaximals", dataCenterCapacity: String = "hostListDecreasing", vmAllocationPolicy: VmAllocationPolicy = new VmAllocationPolicySimple(), vmScheduler: VmScheduler = new VmSchedulerTimeShared()): Double = {

    logger.info("Simulation for Data Center Started....")
    val cloudsim: CloudSim = new CloudSim
    // Creates a Broker
    val broker0: DatacenterBroker = getDatacenterBroker(dataCenterBrokerEnum, cloudsim)

    //Creates a Datacenter with a list of Hosts.
    val dc0: Datacenter = Helper.createDataCenter(cloudsim, dataCenterSpec, dataCenterCharges, dataCenterCapacity, vmAllocationPolicy, vmScheduler, "Mumbai")

    //Creates VMs to run applications.
    val vmList: util.List[Vm] = Helper.createVmList()

    //Creates Cloudlets that represent applications to be run inside a VM.
    val cloudletList: util.List[Cloudlet] = Helper.createCloudLetList()

    broker0.submitVmList(vmList)
    broker0.submitCloudletList(cloudletList)

    /*Starts the simulation and waits all cloudlets to be executed, automatically
    stopping when there is no more events to process.*/
    cloudsim.start

    // Prints results when the simulation is over
    Helper.printSummary(broker0)
  }

  /**
   * generates the DataCenterBroker Object depending on passed enum
   *
   * @param dataCenterBrokerEnum
   * @param cloudSim
   * @return DataCenterBroker Object
   */
  def getDatacenterBroker(dataCenterBrokerEnum: DataCenterBrokerEnum.Value, cloudSim: CloudSim): DatacenterBroker = {

    dataCenterBrokerEnum match {
      case DataCenterBrokerEnum.DatacenterBrokerSimple => new DatacenterBrokerSimple(cloudSim)
      case DataCenterBrokerEnum.DatacenterBrokerBestFit => new DatacenterBrokerBestFit(cloudSim)
      case DataCenterBrokerEnum.DatacenterBrokerFirstFit => new DatacenterBrokerFirstFit(cloudSim)
    }
  }

}
