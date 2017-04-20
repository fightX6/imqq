package com.qq;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qq on 2017/4/20.
 */
public class MyPcap {
    public static void main(String[] args) throws InterruptedException {
        List<PcapIf> alldevs = new ArrayList<PcapIf>(); // Will be filled with
// NICs
        StringBuilder errbuf = new StringBuilder();
        int r = Pcap.findAllDevs(alldevs, errbuf);
        if (r == Pcap.NOT_OK || alldevs.isEmpty()) {
            System.err.printf("Can't read list of devices, error is %s",
                    errbuf.toString());
            return;
        }
        for (PcapIf pif : alldevs) {
            System.out.println(pif.getName());
        }


        PcapIf pif = alldevs.get(0);//select the device which you want to monitor


/***************************************
 * open the device
 ***************************************/
        int snaplen = 64 * 1024; // Capture all packets, no trucation
        int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
        int timeout = 10 * 1000; // 10 seconds in millis
        Pcap pcap = Pcap.openLive(pif.getName(), snaplen, flags, timeout,
                errbuf);


        if (pcap == null) {
            System.err.printf("Error while opening device for capture: "
                    + errbuf.toString());
            return;
        }

/*
         * We have an opened the capture file now time to read packets. We use a
         * MyPcap.loop function to retrieve 10 packets from the file. We supply an
         * annonymous handler which will receive packets as they are read from the
         * offline file by libpcap. We parameterize it with a StringBuilder class.
         * This allows us to pass in any type of object we need inside the our
         * dispatch handler. For this example we are passing in the errorbuf object
         * so we can pass back a string, if we need to. Of course in our example
         * this is not strictly needed since our anonymous class can access errbuf
         * object directly from the enclosing main method as that local variable is
         * marked final allowing anonymous classes access to it.
         */
        pcap.loop(Pcap.LOOP_INFINITE, new JPacketHandler<StringBuilder>() {

            /**
             * We purposely define and allocate our working tcp header (accessor)
             * outside the dispatch function and thus the libpcap loop, as this type
             * of object is reusable and it would be a very big waist of time and
             * resources to allocate it per every dispatch of a packet. We mark it
             * final since we do not plan on allocating any other instances of Tcp.
             */
            final Tcp tcp = new Tcp();


            /*
             * Same thing for our http header
             */
            final Http http = new Http();


            /**
             * Our custom handler that will receive all the packets libpcap will
             * dispatch to us. This handler is inside a libpcap loop and will receive
             * exactly 10 packets as we specified on the MyPcap.loop(10, ...) line
             * above.
             *
             * @param packet
             *          a packet from our capture file
             * @param errbuf
             *          our custom user parameter which we chose to be a StringBuilder
             *          object, but could have chosen anything else we wanted passed
             *          into our handler by libpcap
             */
            public void nextPacket(JPacket packet, StringBuilder errbuf) {

                /*
                 * Here we receive 1 packet at a time from the capture file. We are
                 * going to check if we have a tcp packet and do something with tcp
                 * header. We are actually going to do this twice to show 2 different
                 * ways how we can check if a particular header exists in the packet and
                 * then get that header (peer header definition instance with memory in
                 * the packet) in 2 separate steps.
                 */
                if (packet.hasHeader(Tcp.ID)) {

                    /*
                     * Now get our tcp header definition (accessor) peered with actual
                     * memory that holds the tcp header within the packet.
                     */
                    packet.getHeader(tcp);

                    System.out.printf("tcp.dst_port=%d%n", tcp.destination());
                    System.out.printf("tcp.src_port=%d%n", tcp.source());
                    System.out.printf("tcp.ack=%x%n", tcp.ack());

                }

                /*
                 * An easier way of checking if header exists and peering with memory
                 * can be done using a conveniece method JPacket.hasHeader(? extends
                 * JHeader). This method performs both operations at once returning a
                 * boolean true or false. True means that header exists in the packet
                 * and our tcp header difinition object is peered or false if the header
                 * doesn't exist and no peering was performed.
                 */
                if (packet.hasHeader(tcp)) {
                    //System.out.printf("tcp header::%s%n", tcp.toString());
                }

                /*
                 * A typical and common approach to getting headers from a packet is to
                 * chain them as a condition for the if statement. If we need to work
                 * with both tcp and http headers, for example, we place both of them on
                 * the command line.
                 */
                if (packet.hasHeader(tcp) && packet.hasHeader(http)) {
                    /*
                     * Now we are guarranteed to have both tcp and http header peered. If
                     * the packet only contained tcp segment even though tcp may have http
                     * port number, it still won't show up here since headers appear right
                     * at the beginning of http session.
                     */


                    System.out.printf("http header::%s%n", http);

                    /*
                     * jNetPcap keeps track of frame numbers for us. The number is simply
                     * incremented with every packet scanned.
                     */

                }

                //System.out.printf("frame #%d%n", packet.getFrameNumber());
            }

        }, errbuf);

        /*
        * Last thing to do is close the pcap handle
        */
        pcap.close();
    }
}
