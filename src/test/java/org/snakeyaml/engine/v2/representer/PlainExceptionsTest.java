/**
 * Copyright (c) 2018, http://www.snakeyaml.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.v2.representer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;

import java.util.Arrays;

@Tag("fast")
public class PlainExceptionsTest {

    @Test
    @DisplayName("null literals should be force-escaped")
    public void nullTest() {

        // ESYNC-5961 Some Mulesoft tool also treats NULL as YAML nulls,
        // so we need to escape those as well.

        DumpSettingsBuilder builder = DumpSettings.builder();
        builder
                .setDefaultScalarStyle(ScalarStyle.PLAIN)
                .setDefaultFlowStyle(FlowStyle.BLOCK)
                .setCanonical(false)
                .setSplitLines(false)
                .setMultiLineFlow(true);
        DumpSettings settings = builder.build();
        Dump dump = new Dump(settings);
        Assertions.assertEquals("- 'null'\n" +
                "- 'NULL'\n" +
                "- not-null\n", dump.dumpToString(Arrays.asList("null", "NULL", "not-null")));

    }

}
