# Overview

Simulated the cloud provider scenarios like simulating the data centre which can behave as a PaaS, SaaS 
and IaaS by using the open source simulator project called [cloudSimplus](http://cloudsimplus.org).
Created the data centre assigned host to it; created VM's and assigned a task (cloudlets) to them via 
data broker and provided the various simulation functions whose parameter can be changed according to the 
need to replicate the required real cloud provider scenarios. 

## Instructions

#### Prequesite
- SBT should be installed

#### Running the project
- Clone or download this repository onto your system
- Open the Command Prompt (if using Windows) or the Terminal (if using Linux/Mac) and browse to the project directory
- Build the project and generate the jar file using SBT
```
sbt clean compile test
sbt clean compile run
```
- while simulating the sceanrios you must use only the keys defined in application.conf file else it will not
work as expected(check the configuration Section for details)

## Architectural Overview


This project is divided into following components :

#### 1) IaaS Simulation
#### 2) SaaS Simulation
#### 3) Data Centre Simulations
#### 4) Dynamic VM Scaling

All this simulation has a method which ends with simulate which is used for simulation purpose and 
all the methods have default parameters which can be changed if needed.

### 1) IaaS Simulation: 

This provides the functionality of creating user defined VM's and user defined tasks(cloudlets) to be run on
those VM. User can vary the VM configurations like Processor, RAM, bandwidth, cloudlet allocation policy  and million instructions/sec 
to be executed and  similarly users can also vary the cloudlet configurations like  file I/O size,
no. of process Execution unit and length.

### 2) SaaS Simulation:

It provides the functionality of simulating data centre as a software service provider. In this the user can 
specify the service name whose instance s/he wants to use and the cloud provider will take care of finding 
a suitable host, creating a specific VM on it and then running the instance of that service on top of it. 
So here, everything is taken care by cloud provider,  the user has to specify only the service name. 
User can specify only the service name which are there in the configuration file. 

The configuration of service is composed of 
- VM specific requirements 
- cloudlet specific requirements 
configuration can be changed for simulation purpose by the changing the application.config file.

### 3) Data Centre Simulations: 

Provides the functionality of running the datacentre's with different configurations like how the VM allocation
should happens and which the data broker type to use. These are the scenarios which are only handled by cloud 
providers. In this simulation you can change the capacity of data centre, 
pricing  model of data centre and change technical specifications of data centre like operating system, virtual machine monitor, etc., 
VM allocation policy and VM scheduling policy. 

### 4)Dynamic VM Scaling:

This module helps in simulating the dynamic aspect of cloud. In this cloudlets are dynamically generated and 
submitted to the data broker. The data broker creates the new VM if the capacity of existing VM exceeds the 
threshold (0.7). Delay the destruction of virtual machines by data broker for some time (10), so that  
 if some task comes within time frame, it'll take care of that. For this to happend, we have introduced a delay in data broker object.
For this Used scalable VM which has a functionality of taking a VM creation function which it uses to scale 
the existing VM when its overloaded. 

## Implementation details:

- All the configurations like data centre specification, data centre charges, data centre capacity are all taken from application.config file. These can be changed and accordingly simulation will change. 
- Used a singleton pattern to read configuration from config file. 
- All the simulation methods have default parameter for all of its input variable's. User has to modify only the parameter he neede for his scenario.
- All the core methods are implemented in helper class which is also singleton class. Methods like creation of VM for specific config name, creation of cloudlet, host, and data centre.
- Used enum's for constraining the user input and making sure that they are always valid. For ex: created enum for data centre broker parameter. 
- Created Custom Exception for handling the various exception which occurs due to use of  wrong config key

## Various Simulation Results and its Analysis:

#### 1) Dynamic VM scaling, in this we saw that VM count increases with the count of submitted cloudlets dynamically when the VM reaches its threshold capacity. Intially we have started with only one Cloudlet and one Vm
 - Simulation Code
````
DynamicVmScaling.simulate()
````

- Console Output
```
INFO  *********** Simulation Result ***********
INFO  ----Length-----|------TimeZone------|-------PEs-----|
INFO  Cloudlet    Vm    Vm    DataCenter    Cloudlet    Vm    Exec Time    Wait Time    CPU cost    Bandwidth Cost    Total Cost
INFO      100    400.0    5.5    10.0            1        4    0.36        0            0.03        4.5                4.53
INFO      100    400.0    5.5    10.0            1        4    0.33        0            0.03        4.5                4.53
INFO      100    400.0    5.5    10.0            1        4    0.33        0            0.03        4.5                4.53
INFO      100    400.0    5.5    10.0            1        4    0.33        0            0.03        4.5                4.53
INFO      100    400.0    5.5    10.0            1        4    0.33        0            0.03        4.5                4.53
INFO      100    400.0    5.5    10.0            1        4    0.33        0            0.03        4.5                4.53
INFO      100    400.0    5.5    10.0            1        4    0.33        0            0.03        4.5                4.53
INFO      100    400.0    5.5    10.0            1        4    0.33        0            0.03        4.5                4.53
INFO      100    400.0    5.5    10.0            1        4    0.33        0            0.03        4.5                4.53
INFO  Total cost fo running all the task with these broker: 40.740000000000016
```
#### 2) When we use data centre broker which is sensitive to location it usually try to assign VM to the data centre whose host are in the same time zone as the VM. 
- Simulation Code
````
// making broker to select the datacenter in same timezone
IaaSSimulation.dataCenterLocationSelectivitySimulate(true) 
````
- Console Output
We can observe that Time Zone of Vm and Datacenter are same due to this specific broker way of assignment
```
INFO  *********** Simulation Result ***********
INFO  ----Length-----|------TimeZone------|-------PEs-----|
INFO  Cloudlet    Vm    Vm    DataCenter    Cloudlet    Vm    Exec Time    Wait Time    CPU cost    Bandwidth Cost    Total Cost
INFO      100    100.0    5.5    5.5            1        1    1        0            0.08        4.5                4.58
INFO      300    300.0    5.5    5.5            3        3    1        0            0.08        15                15.08
INFO      300    200.0    5.5    5.5            2        2    1.61        0            0.13        10.5                10.63
INFO  Total cost fo running all the task with these broker: 30.288800000000002

```
#### 3) Increasing the data centre charges increase the price for the customer. this can be seen as we have simulated two data centre's with the same infrastructure model but with different pricing model which resulted in the increased price for the customer. 
##### Data center with minimal charges 
- Simulation Code
````
DataCenterSimulations.simulate(dataCenterCharges = "dataCenterChargesMinimals")
````
- Console Output
```
INFO  *********** Simulation Result ***********
INFO  ----Length-----|------TimeZone------|-------PEs-----|
INFO  Cloudlet    Vm    Vm    DataCenter    Cloudlet    Vm    Exec Time    Wait Time    CPU cost    Bandwidth Cost    Total Cost
INFO      300    200.0    5.5    5.5            2        2    1.55        0            0.09        3.5                3.59
INFO      400    300.0    5.5    5.5            4        3    1.88        0            0.11        7                7.11
INFO      300    100.0    5.5    5.5            3        1    9.22        0            0.55        5                5.55
INFO  Total cost fo running all the task with these broker: 16.25946666666667
```
##### Data center with high charges
- Simulation Code
````
 DataCenterSimulations.simulate(dataCenterCharges = "dataCenterChargesMaximals")
````
- Console Output
```
INFO  *********** Simulation Result ***********
INFO  ----Length-----|------TimeZone------|-------PEs-----|
INFO  Cloudlet    Vm    Vm    DataCenter    Cloudlet    Vm    Exec Time    Wait Time    CPU cost    Bandwidth Cost    Total Cost
INFO      300    200.0    5.5    5.5            2        2    1.55        0            0.12        10.5                10.62
INFO      400    300.0    5.5    5.5            4        3    1.88        0            0.15        21                21.15
INFO      300    100.0    5.5    5.5            3        1    9.22        0            0.74        15                15.74
INFO  Total cost fo running all the task with these broker: 47.51262222222222	
```
#### 4) Changing the data centre broker type changes the jobs that can be compeleted by the cloud .

##### Data center broker of intstance DatacenterBrokerFirstFit
- Simulation Code
````
// broker which doesnot checks while assigning the Vm to the Host just assigns to the first Host which satisfy the Vm requirement.
// hence end up doing less tasks then submitted due to poor resource management
DataCenterSimulations.simulate(dataCenterBrokerEnum=DataCenterBrokerEnum.DatacenterBrokerFirstFit,dataCenterCapacity="hostListMinimal",vmAllocationPolicy = new VmAllocationPolicyBestFit)

````
- Console Output
```
INFO  ----Length-----|------TimeZone------|-------PEs-----|
INFO  Cloudlet    Vm    Vm    DataCenter    Cloudlet    Vm    Exec Time    Wait Time    CPU cost    Bandwidth Cost    Total Cost
INFO      300    400.0    5.5    5.5            2        4    0.75        0            0.06        10.5                10.56
INFO      300    300.0    5.5    5.5            3        3    1.11        0            0.09        15                15.09
INFO  Total cost fo running all the task with these broker: 25.6488
```

##### Data center broker of intstance DatacenterBrokerBestFit
- Simulation Code
````
// Simulates the data broker which selects the best fit for the provided Vms in terms of host in data center
// hence able to perform all the tasks assigned.
DataCenterSimulations.simulate(dataCenterBrokerEnum = DataCenterBrokerEnum.DatacenterBrokerBestFit,dataCenterCapacity="hostListMinimal",vmAllocationPolicy = new VmAllocationPolicyBestFit)
````
- Console Output
```
INFO  ----Length-----|------TimeZone------|-------PEs-----|
INFO  Cloudlet    Vm    Vm    DataCenter    Cloudlet    Vm    Exec Time    Wait Time    CPU cost    Bandwidth Cost    Total Cost
INFO      300    300.0    5.5    5.5            3        3    1        0            0.08        15                15.08
INFO      400    400.0    5.5    5.5            4        4    1        0            0.08        21                21.08
INFO      300    200.0    5.5    5.5            2        2    1.61        0            0.13        10.5                10.63
INFO  Total cost fo running all the task with these broker: 46.788799999999995
```

### Configuration Details
- <"Config Name"> anything inside this braces can we changed by user as per his need
- Whereever it mentions to provide config name user must specify the config name which is defined in application.config file else
  it will throw expection
- All the config name defined by user should have unique name else it will create an exception at runtime.
- Defining new config object must adhere to its format means all fields are mandatory you can't omit any of them.
- Below are the formats for various config object that are needed for simulation purpose.
#### 1) Data Center Technical Configuration format
##### Format
```json
   <ConfigName> {
  architecture=<architecture name> 
  os=<osName> 
  vmm=<Vmm name> 
 }
```
##### Sample
```json
  dataCenter1{
architecture="x86"
os="Linux"
vmm="VMWare"
}
```

#### 2) Data Center charge configuration format
##### Format
```json
  <data Center Charge Config name>{
     costPerMem=<Number>
     costPerStorage=<Number>
     costPerBw=<Number>
     costPerSec=<Number>
 }
```
##### Sample
```json
dataCenterChargesMinimals{
    costPerMem=.03
    costPerStorage=.001
    costPerBw=.00001
    costPerSec=.06
}
```
#### 3) Data Center Capacity configuration format
##### Format
```json
 <ConfigName>{
 <HostConfigName1>=<Number of hosts>
 <HostConfigName2>=<Number of hosts>
 ...as any host as you need
 }
   
```
##### Sample
```json
  hostListDecreasing {
    hostMedium=1
    hostSmall=3
    hostLarge=4
 }
```
#### 4) Host Configuration
##### Format
```json 
 <Config Name>{
    ram=<Size in MB>
    bw=<Size in MB>
    storage=<Size in MB>
    peDetail=<Config Name of Process Exceution>
 }
```
##### Sample
```json
hostLarge{
    ram=8192
    bw=800
    storage=1000000
    peDetail="PELarge"

```

#### 5) Process Execution Configuration
##### Format
```json 
  <PEName>{
     mipsCapacity=<Size in millions Instructions>
     count=<Number of processors>
 }
```
##### Sample
```json
PEMedium{
    mipsCapacity=300
    count=10
}
```

#### 6) Vm Configuration
##### Format
```json
<Vm Config Name >{
     mipsCapacity=<Number>
     numberOfPes=<Number of PE Units>
     ram=<Ram size in MB>
     bw=<bandwidth in MB>
 }
```
##### Sample
```json
vm5{
    mipsCapacity=500
    numberOfPes=5
    ram=5120
    bw=500
}
```

#### 7) Cloudlet Configuration 
##### Format
```json
  <cloudlet_Config_Name> {
       length=<million instructions count>
       pesNumber=<number of process execution units>
       fileSize=<size of input file>
       outFileSize=<size of outputFile>
  }
```
##### Sample
```json
cloudlet1 {
    length=100
    pesNumber=1
    fileSize=100000
    outFileSize=50000
}
```
#### 8) Vm List Configuration 
##### Format
```json 
 <Vm List Config name> = ["<Vm1_config_name>","<Vm2_config_name>",<Vm1_config_name>]
```
##### Sample
```json
vmIncreaseList=["vm1","vm2","vm3","vm4","vm5","vm6"]
```

#### 8) Cloudlet List configuration 
##### Format
```json
 <Cloudlet list config name> = ["<cloudlet_config_name 1>",<cloudlet_config_name 2>,<cloudlet_config_name 1>]
```
##### Sample
```json
cloudletIncreaseList = ["cloudlet1","cloudlet2","cloudlet3"]
```

#### 9) Service Configuration 
##### Format
```json 
<service Config Name>{
     cloudlets=["<cloudlet_config_name 1>",<cloudlet_config_name 2>,<cloudlet_config_name 1>]
     vms=["<Vm1_config_name>","<Vm2_config_name>",<Vm1_config_name>]
     dataCenterSpec="<data center technical config name>"
     dataCenterCharges="<data Center Charges config name>"
     dataCenterCapacity="<data center capacity config name>"
 }
```
##### Sample
```json
service1{
    cloudlets=["cloudlet1","cloudlet2","cloudlet3"]
    vms=["vm3","vm2","vm1"]
    dataCenterSpec="dataCenter1"
    dataCenterCharges="dataCenterChargesMinimals"
    dataCenterCapacity="hostListDecreasing"
}

```

