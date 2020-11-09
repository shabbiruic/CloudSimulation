package Configurations

/*
enum to be use to get specfic type of databroker object to be created during simulations
 */
object DataCenterBrokerEnum extends Enumeration {
  val DatacenterBrokerSimple, DatacenterBrokerBestFit, DatacenterBrokerFirstFit = Value
}
