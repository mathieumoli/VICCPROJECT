# Notes about the project

## The team

- Loryn Fontaine: loryn.fontaine@etu.unice.fr
- Nicolas Hory: nicolas.hory@etu.unice.fr
- Mathieu Molinengo: mathieu.molinengo@etu.unice.fr

## Comments

### Anti Affinity algorithm

For this  algorithm, the impact on the cluster hosting capacity is that more hosts are going to be used. 
The reason is that the available capacity of a host is no more the only criteria in order to allocate a VM.
Because of this, some hosts would have the capacity for one more VM, but the affinity criteria is preventing
that, and another host has to be used.

### Fault-tolerance for standalone VMs


### Load Balancing
The algorithm performing the best in terms of reduction of SLA violations is the worstFit one. The reason is 
that we allocate VM's to the hosts with the most Mips and RAM. By doing this, the probability of violating
SLA because of a lack of available resources is lower than with the nextFit algorithm which only considers
the last host which allocated a vm.

### Performance Satisfaction
This algorithm is effective because we can see by executing it that there are no reported penalties, which
means that we didn't try to allocate a VM to a host with not enough capacity for it.