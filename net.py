from mininet.net import Mininet
from mininet.node import Host, OVSSwitch
from mininet.cli import CLI
from mininet.node import RemoteController
from mininet.link import TCLink

def create_topology():
    net = Mininet(controller=RemoteController, link=TCLink)
    
    net.addController('c1', controller=RemoteController, ip='127.0.0.1', port=6653)

    # Create switches
    switch1 = net.addSwitch('s1', dpi = 1,protocol = "OpenFlow13")
    switch2 = net.addSwitch('s2', dpi = 2,protocol = "OpenFlow13")
    switch3 = net.addSwitch('s3', dpi = 3,protocol = "OpenFlow13")
    switch4 = net.addSwitch('s4', dpi = 4,protocol = "OpenFlow13")
    switch5 = net.addSwitch('s5', dpi = 5,protocol = "OpenFlow13")
    switch6 = net.addSwitch('s6', dpi = 6,protocol = "OpenFlow13")
    switch7 = net.addSwitch('s7', dpi = 7,protocol = "OpenFlow13")
    switch8 = net.addSwitch('s8', dpi = 8,protocol = "OpenFlow13")

    # Create hosts
    host1 = net.addHost('h1')
    host2 = net.addHost('h2')
    host3 = net.addHost('h3')
    host4 = net.addHost('h4')
    host5 = net.addHost('h5')
    host6 = net.addHost('h6')
    host7 = net.addHost('h7')
    host8 = net.addHost('h8')

    # Connect switches
    net.addLink(switch1, switch3, bw=1000, delay='1ms')
    net.addLink(switch1, switch8, bw=1000, delay='1ms')
    net.addLink(switch3, switch8, bw=1000, delay='1ms')
    net.addLink(switch3, switch6, bw=1000, delay='1ms')
    net.addLink(switch3, switch4, bw=1000, delay='1ms')
    net.addLink(switch6, switch8, bw=1000, delay='1ms')
    net.addLink(switch6, switch5, bw=1000, delay='1ms')
    net.addLink(switch8, switch7, bw=1000, delay='1ms')
    net.addLink(switch5, switch2, bw=1000, delay='1ms')
    net.addLink(switch5, switch4, bw=1000, delay='1ms')
    net.addLink(switch5, switch7, bw=1000, delay='1ms')
    net.addLink(switch2, switch4, bw=1000, delay='1ms')
    net.addLink(switch2, switch7, bw=1000, delay='1ms')
    net.addLink(switch4, switch7, bw=1000, delay='1ms')

    # Connect hosts to switches
    net.addLink(host1, switch1, bw=1000, delay='1ms')
    net.addLink(host2, switch2, bw=1000, delay='1ms')
    net.addLink(host3, switch3, bw=1000, delay='1ms')
    net.addLink(host4, switch4, bw=1000, delay='1ms')
    net.addLink(host5, switch5, bw=1000, delay='1ms')
    net.addLink(host6, switch6, bw=1000, delay='1ms')
    net.addLink(host7, switch7, bw=1000, delay='1ms')
    net.addLink(host8, switch8, bw=1000, delay='1ms')

    # Start the network
    net.start()

    # Enable ICMP on hosts for ping
    #for host in net.hosts:
    #    host.cmd('sysctl -w net.ipv4.icmp_echo_ignore_all=0')

    # Enter the Mininet command line interface
    CLI(net)

    # Stop the network
    net.stop()

if __name__ == '__main__':
    create_topology()
