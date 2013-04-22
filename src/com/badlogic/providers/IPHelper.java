package com.badlogic.providers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.StringTokenizer;
import java.util.Vector;

public class IPHelper {
    public static InetAddress[] getAllOnline() {
        Vector v = new Vector(50);
        try {
            Process process = Runtime.getRuntime().exec("arp -a");
            InputStreamReader inputStr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(inputStr);
            String temp = "";
            while ((temp = br.readLine()) != null) {
                StringTokenizer tokens = new StringTokenizer(temp);
                String x = tokens.nextToken();
                InetAddress add = InetAddress.getByName(x  );
                // System.out.println(x);
                v.add(add);
                // System.out.println(add);
            }
            v.add(InetAddress.getLocalHost());
            process.destroy();
            br.close();
            inputStr.close();
        } catch (Exception e) {
            System.out.println("可能是网络不可用。");
            e.printStackTrace();
        }
        int cap = v.size();
        InetAddress[] addrs = new InetAddress[cap];
        for (int i = 0; i < cap; i++) {
            addrs[i] = (InetAddress) v.elementAt(i);
            // System.out.println(addrs[i]);
        }
        return addrs;

    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        InetAddress[] ips = getAllOnline();
        for (InetAddress i : ips) {
            System.out.print(i.getHostName());
        }
        
    }

}
