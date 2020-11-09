package Simulations

import java.util

import Simulations.Helper.logger
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerBestFit}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletScheduler, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModel, UtilizationModelDynamic}
import org.cloudbus.cloudsim.vms.Vm
import org.slf4j.{Logger, LoggerFactory}

/**
 * here all the simulations related to IaaS can be performed.
 * like you can create diff Vm configurations, diff Cloudlet Configuration etc.
 */
object IaaSSimulation {

  val logger: Logger = LoggerFactory.getLogger(IaaSSimulation.getClass)

  /**
   * Simulate datacenter creation by providing different inputs
   *
   * @param dataCenterSelectivity specify whether you want to assign Vm to near by data center in terms of location
   * @param vmListConfig          Vm Config name which contains list of vm config names
   * @param cloudletListConfig    Vm cloudlet name which contains list of cloudlet config names
   * @param cloudletScheduler     schedular that Vm will use for allocation
   * @param place                 location where host of data center will be kept
   */
  def dataCenterLocationSelectivitySimulate(dataCenterSelectivity: Boolean, vmListConfig: String = "vmIncreaseList", cloudletListConfig: String = "cloudletIncreaseList", cloudletScheduler: CloudletScheduler = new CloudletSchedulerTimeShared, place: String = "Mumbai"): DatacenterBroker = {

    logger.info("Simulation for data center selectivity started....")
    //Creates VMs to run applications.
    val vmList: util.List[Vm] = Helper.createVmList(vmListConfig, cloudletScheduler, place)

    //Creates Cloudlets that represent applications to be run inside a VM.
    val cloudletList: util.List[Cloudlet] = Helper.createCloudLetList(cloudletListConfig, new UtilizationModelDynamic(1))

    //    creates the broker and data center with specified vms and cloudlets
    createBrokerWithDataCenter(dataCenterSelectivity, vmList, cloudletList)

  }


  /**
   * Simulate the IaaS scenarios by providing the different Configurations for Vms and
   * Cloudlets
   *
   * @param utilizationModel   utilization model for RAM,BW and storage of cloudlets
   * @param cloudletScheduler  for scheduling cloudlet to Vms
   * @param vmListConfig       Vm Config name which contains list of vm config names
   * @param cloudletListConfig Cloudlet Config name which contains list of cloudlet config names
   * @param place              location where Vms will be kept
   */
  def simulate(utilizationModel: UtilizationModel = new UtilizationModelDynamic(.7), cloudletScheduler: CloudletScheduler = new CloudletSchedulerTimeShared, vmListConfig: String = "vmIncreaseList", cloudletListConfig: String = "cloudletIncreaseList", place: String = "Mumbai"): DatacenterBroker = {

    logger.info("Simulation for IaaS Started....")
    val vmList: util.List[Vm] = Helper.createVmList(vmListConfig, cloudletScheduler, place)

    //Creates Cloudlets that represent applications to be run inside a VM.
    val cloudletList: util.List[Cloudlet] = Helper.createCloudLetList(cloudletListConfig, utilizationModel)

    createBrokerWithDataCenter(true, vmList, cloudletList)
  }


  /**
   * method to create data Center along with the broker to allocate vm and cloudlets to it
   * and start the simulation
   *
   * @param dataCenterSelectivity
   * @param vmList
   * @param cloudletList
   * @param dataCenterSpecs
   * @param dataCenterCharges
   * @param dataCenterCapacity
   */
  def createBrokerWithDataCenter(dataCenterSelectivity: Boolean, vmList: util.List[Vm], cloudletList: util.List[Cloudlet], dataCenterSpecs: String = "dataCenter1", dataCenterCharges: String = "dataCenterChargesMaximals", dataCenterCapacity: String = "hostListDecreasing"): DatacenterBroker = {

    logger.info("Creating of Data Center Broker for IaaS simulation started....")
    val cloudsim: CloudSim = new CloudSim

    /*Creates a Broker that will act on behalf of a cloud user (customer).*/
    val broker0: DatacenterBroker = new DatacenterBrokerBestFit(cloudsim)
    broker0.setSelectClosestDatacenter(dataCenterSelectivity)

    //Creates a Datacenter with specified configs Hosts.
    val dc0: Datacenter = Helper.createDataCenter(cloudsim, dataCenterSpecs, dataCenterCharges, dataCenterCapacity, new VmAllocationPolicyBestFit(), new VmSchedulerTimeShared(), "Sydney")
    val dc2: Datacenter = Helper.createDataCenter(cloudsim, dataCenterSpecs, dataCenterCharges, dataCenterCapacity, new VmAllocationPolicyBestFit(), new VmSchedulerTimeShared(), "Mumbai")


    //    allocation Vms to dataCenter via broker
    broker0.submitVmList(vmList)
    //    allocation Cloudlets to Vms via broker
    broker0.submitCloudletList(cloudletList)

    /*Starts the simulation and waits all cloudlets to be executed, automatically
    stopping when there is no more events to process.*/
    cloudsim.start

    //Prints results when the simulation is over
    Helper.printSummary(broker0)
    broker0
  }
}
