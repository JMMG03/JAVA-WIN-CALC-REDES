package model;

public record IPv4Result(
        String inputIp,
        String mask,
        int cidr,
        String wildcard,
        String network,
        String broadcast,
        String firstHost,
        String lastHost,
        long usableHosts,
        String ipClass,
        boolean isPrivate
) {}
