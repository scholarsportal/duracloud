/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

/**
 * A utility that provides a simple wait
 *
 * @author Bill Branan
 *         Date: 7/19/13
 */
public class WaitUtil {

    /**
     * Causes the current thread to waits for a given number of seconds.
     *
     * @param seconds - the number of seconds to wait
     */
    public static void wait(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch(InterruptedException e) {
        }
    }

}