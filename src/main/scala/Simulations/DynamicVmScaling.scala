package Simulations

import java.util
import java.util.function.Supplier

import Simulations.Helper._
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerBestFit}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple
import org.cloudsimplus.listeners.EventInfo
import org.slf4j.{Logger, LoggerFactory}

/**
 * class to dynamically create and destroy the vms depending on arrivals of cloudlets
 */
object DynamicVmScaling {

  val logger: Logger = LoggerFactory.getLogger(DynamicVmScaling.getClass)

  val cloudsim: CloudSim = new CloudSim()

  //Creates a Broker
  val broker0: DatacenterBroker = new DatacenterBrokerBestFit(cloudsim)


  /**
   * simulates the scenarios of dyanmically sending the clouldets to the broker
   * and broker scaling up and down the Vm depending on load of Vm
   */
  def simulate(): Int = {

    logger.info("Simulation for Dynamic Vm Scaling started....")

    //creates a data Center with specified resources
    val dc0: Datacenter = Helper.createDataCenter(cloudsim, "dataCenter1", "dataCenterChargesMaximals", "hostListDecreasing", new VmAllocationPolicyBestFit(), new VmSchedulerTimeShared(), "Sydney")

    //    adding a callback function which will get called when constraints are satisfied
    cloudsim.addOnClockTickListener(createCloudletDynamcally)

    //    delaying the Vm destruction so that it can be reused if some cloudlets comes within this time frame
    broker0.setVmDestructionDelay(10)

    //submitting the Vms to broker
    broker0.submitVm(createScalableVms())

    //  submitting the cloudlets to the broker
    broker0.submitCloudlet(createCloudlet())
    cloudsim.start()

    //    print the summary of simulation
    Helper.printSummary(broker0)
    broker0.getCloudletSubmittedList.size()
  }

  /**
   * function which generates the cloudlets dynamically in a specific frequency
   * and submits to broker
   *
   * @param eventInfo
   * @return
   */
  def createCloudletDynamcally(eventInfo: EventInfo) = {

    logger.info("Cloudlet are getting generated....")

    val time: Long = eventInfo.getTime.asInstanceOf[Long]
    if ((time % 10 == 0) && time <= 50) {
      val numberOfCloudlets = 4
      val newCloudlets = new util.ArrayList[Cloudlet](numberOfCloudlets)
      for (i <- 0 until numberOfCloudlets) {
        val cloudlet = createCloudlet
        newCloudlets.add(cloudlet)
      }
      broker0.submitCloudletList(newCloudlets)
    }


  }

  /**
   * creates new cloudlet
   *
   * @return new Cloudlet with basic config
   */
  def createCloudlet(): Cloudlet = {
    Helper.createCloudlet("cloudlet1", new UtilizationModelFull())
  }

  /**
   * create Vm which  horizontally scales them self if they are overloaded.
   *
   * @return
   */
  def createScalableVms(): Vm = {

    logger.info("Self Scaling Vms are about to generated....")

    val vm: Vm = createBasicVm()

    val horizontalScaling = new HorizontalVmScalingSimple
    horizontalScaling
      .setVmSupplier(new Supplier[Vm] {
        override def get(): Vm = createBasicVm()
      })
      .setOverloadPredicate(this.isVmOverloaded)
    vm.setHorizontalScaling(horizontalScaling)
  }

  /**
   *
   * @return Vm with basic configuration
   */
  def createBasicVm(): Vm = {
    createVm("vm4", new CloudletSchedulerTimeShared, "Mumbai");
  }

  /**
   * check whether Vm is overloaded or not
   *
   * @param vm
   * @return true if overlaoded
   */
  def isVmOverloaded(vm: Vm): Boolean = {
    vm.getCpuPercentUtilization > 0.7
  }

}
