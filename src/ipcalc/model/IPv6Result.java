package model;

import java.math.BigInteger;

public record IPv6Result(
        String inputIp,
        int prefix,
        String network,
        BigInteger totalAddresses,
        String type
) {}
