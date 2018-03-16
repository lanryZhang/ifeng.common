package com.ifeng.common.misc;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class NetAddressUtil {
	public static List<String> getLocalAddress() throws SocketException {
		List<String> result = new ArrayList<String>();

		Enumeration<NetworkInterface> neEnumeration = NetworkInterface.getNetworkInterfaces();
		while (neEnumeration.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) neEnumeration.nextElement();

			Enumeration<InetAddress> e2 = ni.getInetAddresses();
			while (e2.hasMoreElements()) {
				InetAddress ia = (InetAddress) e2.nextElement();
				if (ia instanceof Inet6Address)
					continue; 
				if (!ia.getHostAddress().equals("127.0.0.1")){
					result.add(ia.getHostAddress());
				}
			}
		}
		return result;
	}

	public static String getLocalAddress(String seperator) throws SocketException {
		return StringUtils.join(getLocalAddress(), "/");
	}
}
