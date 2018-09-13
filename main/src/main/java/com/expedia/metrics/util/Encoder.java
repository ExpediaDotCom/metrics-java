/*
 * Copyright 2018 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.metrics.util;

public class Encoder {
    private static final char[] HEX_DIGITS = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    /**
     * Appends hex code of bytes to builder.
     * @param builder
     * @param bytes
     */
    public static void encodeHex(StringBuilder builder, byte[] bytes) {
        for (byte b : bytes) {
            builder.append(HEX_DIGITS[(0xF0 & b) >>> 4])
                .append(HEX_DIGITS[0x0F & b]);
        }
    }
}
