import Configurations.DataCenterBrokerEnum
import Simulations.{DataCenterSimulations, DynamicVmScaling, IaaSSimulation, SaaSSimulation}
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyBestFit, VmAllocationPolicyWorstFit}
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic

// Remove the single line comment for executing sample simulations
object SimulatorRunner {

  def main(args: Array[String]): Unit = {

    /*    Uncomment below line for Dynamic Simulation to start*/
    //        DynamicVmScaling.simulate()

    /*    Uncomment below line for SaaS Simulation to start
      Check method docs for parameters specifications*/
    //        SaaSSimulation.simulate("service1","Mumbai")

    /*    Uncomment below line for DataCenter Location Selectivity Simulation to start
             making broker to select the datacenter in same timezone*/
    //        IaaSSimulation.dataCenterLocationSelectivitySimulate(true)

    /*
            Uncomment below line for DataCenter Simulation without Selectivity to start
    */
    //        IaaSSimulation.dataCenterLocationSelectivitySimulate(false)
    /*
            Uncomment below line for IaaS Simulation to start
            Simulation with utilization of Vm only 40%*/
    //        IaaSSimulation.simulate(utilizationModel = new UtilizationModelDynamic(.4))
    /*
            IaaS simulation with diff cloudlet schedular*/
    //        IaaSSimulation.simulate(cloudletScheduler = new CloudletSchedulerSpaceShared,utilizationModel = new UtilizationModelDynamic(.4))

    /*    Uncomment below line for Data Center Simulation to start
    Default simulation*/
    //        DataCenterSimulations.simulate()
    /*
        By changing the Data center Compute power*/
    //        DataCenterSimulations.simulate(dataCenterCapacity = "hostListMinimal")
    /*
            By changing the Data Center price model*/
    //        DataCenterSimulations.simulate(dataCenterCharges = "dataCenterChargesMinimals")
    //        DataCenterSimulations.simulate(dataCenterCharges = "dataCenterChargesMaximals")
    /*
            By Changing Vm Allocation Policy */
    //        DataCenterSimulations.simulate(dataCenterCapacity="hostListMinimal",vmAllocationPolicy = new VmAllocationPolicyBestFit)

    /*
            By Changing the Data broker type and Vm Allocation Policy
    */
    //        DataCenterSimulations.simulate(dataCenterBrokerEnum = DataCenterBrokerEnum.DatacenterBrokerBestFit,dataCenterCapacity="hostListMinimal",vmAllocationPolicy = new VmAllocationPolicyBestFit)
    //        DataCenterSimulations.simulate(dataCenterBrokerEnum=DataCenterBrokerEnum.DatacenterBrokerFirstFit,dataCenterCapacity="hostListMinimal",vmAllocationPolicy = new VmAllocationPolicyBestFit)
  }
}
