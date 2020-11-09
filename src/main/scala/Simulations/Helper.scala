package Simulations

import java.text.DecimalFormat
import java.util

import Configurations.Myconfig
import com.typesafe.config.Config
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletScheduler, CloudletSchedulerCompletelyFair, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmScheduler, VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModel, UtilizationModelDynamic}
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection._
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.{BufferHasAsJava, CollectionHasAsScala}

/**
 * Class which contains all the helper method need for simulations of SaaS, PaaS and, IaaS
 */
object Helper {

  val logger: Logger = LoggerFactory.getLogger(Helper.getClass)

  /**
   * it converts the place into time Zone
   *
   * @param place specify the location name ex: Mumbai, Sydney
   * @return TimeZone which is Double type
   */
  def getTimeZone(place: String) = {
    Myconfig.getObject("timeZone").getDouble(place)
  }

  /**
   * Method to create Vm of specified Configuration
   *
   * @param vmConfig          configuration of Vm
   * @param cloudletScheduler policy which generated Vm should respect while cloudlet allocations
   * @param place             location where this Vm will get Generated
   * @return Generated Vm
   */
  def createVm(vmConfig: String, cloudletScheduler: CloudletScheduler, place: String): Vm = {
    logger.info("creation of Vm is called....")
      val vmDetails: Config = Myconfig.getObject(vmConfig)
      new VmSimple(vmDetails.getDouble("mipsCapacity"), vmDetails.getLong("numberOfPes"), cloudletScheduler)
        .setTimeZone(getTimeZone(place))
        .setRam(vmDetails.getLong("ram"))
        .setBw(vmDetails.getLong("bw"))
  }

  /**
   * Creation of host with specified config
   *
   * @param hostConfig : name of host configuration to be used for host creation
   * @param hostId
   * @param vmSchedule : vm Schedule that host will follow
   * @return Generated Host with given Config
   */
  def createHost(hostConfig: String, hostId: Int, vmSchedule: VmScheduler): Host = {

    var peList = new ListBuffer[Pe]()
    val hostDetail: Config = Myconfig.getObject(hostConfig)
    val peDetails: Config = Myconfig.getObject(hostDetail.getString("peDetail"))
    var i = 0;

    //List of Host's CPUs (Processing Elements, PEs)
    for (i <- 1 to peDetails.getInt("count")) {
      //Uses a PeProvisionerSimple by default to provision PEs for VMs
      logger.info("creation of PE....")
      peList += new PeSimple(peDetails.getInt("mipsCapacity"))
    }

    /*
      Uses ResourceProvisionerSimple by default for RAM and BW provisioning
      and VmSchedulerSpaceShared for VM scheduling.
      */
    logger.info("creation of host started....")
    val host: Host = new HostSimple(hostDetail.getInt("ram"), hostDetail.getInt("bw"),
      hostDetail.getInt("storage"), peList.asJava)
    host.setId(hostId); //hostId

    host.setVmScheduler(vmSchedule)
    return host;
  }

  /**
   *
   * @param cloudSim
   * @param dataCenterSpec     technical configuration of data center
   * @param dataCenterCharges  pricing configuration of data center
   * @param dataCenterCapacity host capacity configuration of data center
   * @param vmPolicy           policy the host of data center will use for Vm allocation
   * @param vmSchedule
   * @param place              location where host will be kept
   * @return
   */
  def createDataCenter(cloudSim: CloudSim, dataCenterSpec: String, dataCenterCharges: String, dataCenterCapacity: String, vmPolicy: VmAllocationPolicy, vmSchedule: VmScheduler, place: String): Datacenter = {
    logger.info("creation of host DataCenter started....")
    val hostDetail: Config = Myconfig.getObject(dataCenterCapacity)
    val characteristics: Config = Myconfig.getObject(dataCenterSpec)
    val charges: Config = Myconfig.getObject(dataCenterCharges)
    var hostList = ListBuffer[Host]();
    var hostCount: Int = 0
    hostDetail.entrySet().forEach(i => {
      for (index <- 1 to i.getValue.unwrapped().asInstanceOf[Int]) {

        hostList.addOne(createHost(i.getKey, hostCount, getVmSchedular(vmSchedule)))
        hostCount += 1
      }
    })
    val dc: Datacenter = new DatacenterSimple(cloudSim, hostList.asJava, vmPolicy)

    dc.setTimeZone(getTimeZone(place))

    logger.info("setting Data Center Characteristics....")
    dc.getCharacteristics
      .setArchitecture(characteristics.getString("architecture"))
      .setOs(characteristics.getString("os"))
      .setVmm(characteristics.getString("vmm"))
      .setCostPerBw(charges.getDouble("costPerBw"))
      .setCostPerStorage(charges.getDouble("costPerStorage"))
      .setCostPerMem(charges.getDouble("costPerMem"))
      .setCostPerSecond(charges.getDouble("costPerSec"))

    return dc
  }

  /**
   * Generates of cloudlet
   *
   * @param cloudlet configuration of cloudlet to be produce
   * @param um       utilization model for RAM,BW and storage
   * @return new Cloudlet with specified config
   */
  def createCloudlet(cloudlet: String, um: UtilizationModel): Cloudlet = {

    logger.info("creation of Cloudlet started....")
    val clConfig: Config = Myconfig.getObject(cloudlet)
    new CloudletSimple(clConfig.getLong("length"), clConfig.getInt("pesNumber"), um)
      .setFileSize(clConfig.getLong("fileSize"))
      .setOutputSize(clConfig.getLong("outFileSize"))
  }

  /**
   * Generates list of Vms
   *
   * @param vmListConfig      config which will specify the list of vm config to be generated
   * @param cloudletScheduler for scheduling cloudlet to Vms
   * @param place             location where host will be kept
   * @return generated Vm list
   */
  def createVmList(vmListConfig: String = "vmIncreaseList", cloudletScheduler: CloudletScheduler = new CloudletSchedulerTimeShared, place: String = "Mumbai"): util.List[Vm] = {
    logger.info("creation of Vm list started....")
    val vmNames: util.List[String] = Myconfig.getStringList(vmListConfig)
    coreCreateVmList(vmNames, cloudletScheduler, place)
  }

  /**
   * Generates list of Vms
   *
   * @param vmNames           list of vm config names
   * @param cloudletScheduler for scheduling cloudlet to Vms
   * @param place             location where Vm will be kept
   * @return generated Vm list
   */
  def coreCreateVmList(vmNames: util.List[String], cloudletScheduler: CloudletScheduler, place: String): util.List[Vm] = {

    var vmList = ListBuffer[Vm]()
    vmNames.forEach(name => vmList.addOne(createVm(name, getCloudletSchedular(cloudletScheduler), place)))
    return vmList.asJava
  }

  /**
   * Generates list of cloudlets
   *
   * @param clNames          list of cloudlet config names
   * @param utilizationModel utilization model for RAM,BW and storage
   * @return new Cloudlets with specified config
   */
  def coreCreateCloudLetList(clNames: util.List[String], utilizationModel: UtilizationModel): util.List[Cloudlet] = {
    var clList = ListBuffer[Cloudlet]()
    clNames.forEach { name =>
      clList.addOne(createCloudlet(name, utilizationModel))
    }
    return clList.asJava
  }

  /**
   * Generates list of cloudlets
   *
   * @param clListConfig     config name which will specify names to cloudlet config to be used for creation
   * @param utilizationModel utilization model for RAM,BW and storage
   * @return new Cloudlets with specified config
   */
  def createCloudLetList(clListConfig: String = "cloudletDecreasingList", utilizationModel: UtilizationModel = new UtilizationModelDynamic(1)): util.List[Cloudlet] = {

    logger.info("creation of CloudLet List started....")
    val clNames: util.List[String] = Myconfig.getStringList(clListConfig)
    coreCreateCloudLetList(clNames, utilizationModel)

  }

  /**
   * create new object of cloudletScheduler
   *
   * @param cloudletScheduler type of cloudletScheduler object you want to create
   * @return cloudletScheduler object
   */
  def getCloudletSchedular(cloudletScheduler: CloudletScheduler): CloudletScheduler = {
    cloudletScheduler match {
      case cloudletScheduler: CloudletSchedulerTimeShared => new CloudletSchedulerTimeShared
      case cloudletScheduler: CloudletSchedulerSpaceShared => new CloudletSchedulerSpaceShared
      case cloudletScheduler: CloudletSchedulerCompletelyFair => new CloudletSchedulerCompletelyFair
    }

  }

  /**
   * create new object of VmScheduler
   *
   * @param vmScheduler type of vmScheduler to create
   * @return new vmScheduler object
   */
  def getVmSchedular(vmScheduler: VmScheduler): VmScheduler = {
    vmScheduler match {
      case vmScheduler: VmSchedulerSpaceShared => new VmSchedulerSpaceShared()
      case vmScheduler: VmSchedulerTimeShared => new VmSchedulerTimeShared()
    }
  }

  /**
   * prints the summmary of whole simulation i.e price, where cloudlet got assigned and which Vm
   * and how much time it took for execution etc.
   *
   * @param broker broker whose cloudlets exceution summary to want to view
   */
  def printSummary(broker: DatacenterBroker): Double = {

    val cloudletList = broker.getCloudletFinishedList.asInstanceOf[java.util.List[Cloudlet]].asScala

    val padding = "    "
    logger.info("*********** Simulation Result ***********")
    logger.info("----Length-----|------TimeZone------|-------PEs-----|")
    logger.info("Cloudlet" + padding + "Vm" + padding + "Vm" + padding + "DataCenter" + padding + "Cloudlet" + padding + "Vm" + padding + "Exec Time" + padding + "Wait Time" + padding + "CPU cost" + padding + "Bandwidth Cost" + padding + "Total Cost")

    var totalCost: Double = 0
    val dft = new DecimalFormat("###.##")
    cloudletList.map { cloudlet: Cloudlet =>

      val host = cloudlet.getVm.getHost
      logger.info(padding + cloudlet.getLength + padding + cloudlet.getVm.getMips + padding + cloudlet.getVm.getTimeZone + padding + host.getDatacenter.getTimeZone + padding +
        padding + padding + cloudlet.getNumberOfPes + padding + padding + cloudlet.getVm.getNumberOfPes + padding + dft.format(cloudlet.getActualCpuTime) + padding + padding + dft.format(cloudlet.getWaitingTime)
        + padding + padding + padding + dft.format(cloudlet.getCostPerSec * cloudlet.getActualCpuTime) + padding + padding +
        dft.format(cloudlet.getAccumulatedBwCost + cloudlet.getCostPerBw * cloudlet.getOutputSize) + padding + padding + padding + padding + dft.format(cloudlet.getTotalCost))

      totalCost += cloudlet.getTotalCost
    }
    logger.info("Total cost fo running all the task with these broker: " + totalCost)
    totalCost
  }
}
