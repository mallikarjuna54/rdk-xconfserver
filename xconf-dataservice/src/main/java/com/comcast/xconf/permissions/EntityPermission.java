/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.comcast.xconf.permissions;

public final class EntityPermission {

    private String readAll;

    private String readStb;

    private String readXhome;

    private String writeAll;

    private String writeStb;

    private String writeXhome;

    private EntityPermission(Builder builder) {
        if (builder.readAll == null || builder.readStb == null || builder.readXhome == null
                || builder.writeAll == null || builder.writeStb == null || builder.writeXhome == null) {
            throw new IllegalArgumentException("Any field should not be null");
        }

        this.readAll = builder.readAll;
        this.readStb = builder.readStb;
        this.readXhome = builder.readXhome;

        this.writeAll = builder.writeAll;
        this.writeStb = builder.writeStb;
        this.writeXhome = builder.writeXhome;
    }

    public String getReadAll() {
        return readAll;
    }

    public String getReadStb() {
        return readStb;
    }

    public String getReadXhome() {
        return readXhome;
    }

    public String getWriteAll() {
        return writeAll;
    }

    public String getWriteStb() {
        return writeStb;
    }

    public String getWriteXhome() {
        return writeXhome;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EntityPermission{");
        sb.append("readAll='").append(readAll).append('\'');
        sb.append(", readStb='").append(readStb).append('\'');
        sb.append(", readXhome='").append(readXhome).append('\'');
        sb.append(", writeAll='").append(writeAll).append('\'');
        sb.append(", writeStb='").append(writeStb).append('\'');
        sb.append(", writeXhome='").append(writeXhome).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private String readAll;

        private String readStb;

        private String readXhome;

        private String writeAll;

        private String writeStb;

        private String writeXhome;

        public Builder() {}

        public Builder(String readAll, String readStb, String readXhome, String writeAll, String writeStb, String writeXhome) {

            this.readAll = readAll;
            this.readStb = readStb;
            this.readXhome = readXhome;
            this.writeAll = writeAll;
            this.writeStb = writeStb;
            this.writeXhome = writeXhome;
        }

        public Builder setReadAll(String readAll) {
            this.readAll = readAll;
            return this;
        }

        public Builder setReadStb(String readStb) {
            this.readStb = readStb;
            return this;
        }

        public Builder setReadXhome(String readXhome) {
            this.readXhome = readXhome;
            return this;
        }

        public Builder setWriteAll(String writeAll) {
            this.writeAll = writeAll;
            return this;
        }

        public Builder setWriteStb(String writeStb) {
            this.writeStb = writeStb;
            return this;
        }

        public Builder setWriteXhome(String writeXhome) {
            this.writeXhome = writeXhome;
            return this;
        }

        public EntityPermission build() {
            return new EntityPermission(this);
        }
    }
}
