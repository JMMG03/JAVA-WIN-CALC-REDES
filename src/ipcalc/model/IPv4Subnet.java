package model;

public record IPv4Subnet(
        int index,
        String cidr,          // p.ej. 192.168.1.0/26
        String network,
        String broadcast,
        String firstHost,
        String lastHost,
        long usableHosts
) {}
