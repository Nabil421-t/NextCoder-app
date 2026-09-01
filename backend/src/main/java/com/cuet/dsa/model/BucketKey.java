//package com.cuet.dsa.model;
//
///**
// * Small helper so we always build Redis keys the same way everywhere.
// * Stripped of IP matching logic to maintain strict user-level partitioning.
// */
//public class BucketKey {
//
//    public static String forUser(long userId) {
//        return "rate:user:" + userId;
//    }
//
//    public static String forUserEndpoint(long userId, String endpoint) {
//        return "rate:user:" + userId + ":endpoint:" + endpoint;
//    }
//}