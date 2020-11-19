/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/

#include "szl_sublist.h"
#include <string.h>


// Create an empty NULL-struct
static const plc4c_s7_read_write_szl_sublist plc4c_s7_read_write_szl_sublist_null_const;

plc4c_s7_read_write_szl_sublist plc4c_s7_read_write_szl_sublist_null() {
  return plc4c_s7_read_write_szl_sublist_null_const;
}

plc4c_s7_read_write_szl_sublist plc4c_s7_read_write_szl_sublist_value_of(char* value_string) {
    if(strcmp(value_string, "MODULE_IDENTIFICATION") == 0) {
        return plc4c_s7_read_write_szl_sublist_MODULE_IDENTIFICATION;
    }
    if(strcmp(value_string, "CPU_FEATURES") == 0) {
        return plc4c_s7_read_write_szl_sublist_CPU_FEATURES;
    }
    if(strcmp(value_string, "USER_MEMORY_AREA") == 0) {
        return plc4c_s7_read_write_szl_sublist_USER_MEMORY_AREA;
    }
    if(strcmp(value_string, "SYSTEM_AREAS") == 0) {
        return plc4c_s7_read_write_szl_sublist_SYSTEM_AREAS;
    }
    if(strcmp(value_string, "BLOCK_TYPES") == 0) {
        return plc4c_s7_read_write_szl_sublist_BLOCK_TYPES;
    }
    if(strcmp(value_string, "STATUS_MODULE_LEDS") == 0) {
        return plc4c_s7_read_write_szl_sublist_STATUS_MODULE_LEDS;
    }
    if(strcmp(value_string, "COMPONENT_IDENTIFICATION") == 0) {
        return plc4c_s7_read_write_szl_sublist_COMPONENT_IDENTIFICATION;
    }
    if(strcmp(value_string, "INTERRUPT_STATUS") == 0) {
        return plc4c_s7_read_write_szl_sublist_INTERRUPT_STATUS;
    }
    if(strcmp(value_string, "ASSIGNMENT_BETWEEN_PROCESS_IMAGE_PARTITIONS_AND_OBS") == 0) {
        return plc4c_s7_read_write_szl_sublist_ASSIGNMENT_BETWEEN_PROCESS_IMAGE_PARTITIONS_AND_OBS;
    }
    if(strcmp(value_string, "COMMUNICATION_STATUS_DATA") == 0) {
        return plc4c_s7_read_write_szl_sublist_COMMUNICATION_STATUS_DATA;
    }
    if(strcmp(value_string, "STATUS_SINGLE_MODULE_LED") == 0) {
        return plc4c_s7_read_write_szl_sublist_STATUS_SINGLE_MODULE_LED;
    }
    if(strcmp(value_string, "DP_MASTER_SYSTEM_INFORMATION") == 0) {
        return plc4c_s7_read_write_szl_sublist_DP_MASTER_SYSTEM_INFORMATION;
    }
    if(strcmp(value_string, "MODULE_STATUS_INFORMATION") == 0) {
        return plc4c_s7_read_write_szl_sublist_MODULE_STATUS_INFORMATION;
    }
    if(strcmp(value_string, "RACK_OR_STATION_STATUS_INFORMATION") == 0) {
        return plc4c_s7_read_write_szl_sublist_RACK_OR_STATION_STATUS_INFORMATION;
    }
    if(strcmp(value_string, "RACK_OR_STATION_STATUS_INFORMATION_2") == 0) {
        return plc4c_s7_read_write_szl_sublist_RACK_OR_STATION_STATUS_INFORMATION_2;
    }
    if(strcmp(value_string, "ADDITIONAL_DP_MASTER_SYSTEM_OR_PROFINET_IO_SYSTEM_INFORMATION") == 0) {
        return plc4c_s7_read_write_szl_sublist_ADDITIONAL_DP_MASTER_SYSTEM_OR_PROFINET_IO_SYSTEM_INFORMATION;
    }
    if(strcmp(value_string, "MODULE_STATUS_INFORMATION_PROFINET_IO_AND_PROFIBUS_DP") == 0) {
        return plc4c_s7_read_write_szl_sublist_MODULE_STATUS_INFORMATION_PROFINET_IO_AND_PROFIBUS_DP;
    }
    if(strcmp(value_string, "DIAGNOSTIC_BUFFER") == 0) {
        return plc4c_s7_read_write_szl_sublist_DIAGNOSTIC_BUFFER;
    }
    if(strcmp(value_string, "MODULE_DIAGNOSTIC_DATA") == 0) {
        return plc4c_s7_read_write_szl_sublist_MODULE_DIAGNOSTIC_DATA;
    }
    return -1;
}

int plc4c_s7_read_write_szl_sublist_num_values() {
  return 19;
}

plc4c_s7_read_write_szl_sublist plc4c_s7_read_write_szl_sublist_value_for_index(int index) {
    switch(index) {
      case 0: {
        return plc4c_s7_read_write_szl_sublist_MODULE_IDENTIFICATION;
      }
      case 1: {
        return plc4c_s7_read_write_szl_sublist_CPU_FEATURES;
      }
      case 2: {
        return plc4c_s7_read_write_szl_sublist_USER_MEMORY_AREA;
      }
      case 3: {
        return plc4c_s7_read_write_szl_sublist_SYSTEM_AREAS;
      }
      case 4: {
        return plc4c_s7_read_write_szl_sublist_BLOCK_TYPES;
      }
      case 5: {
        return plc4c_s7_read_write_szl_sublist_STATUS_MODULE_LEDS;
      }
      case 6: {
        return plc4c_s7_read_write_szl_sublist_COMPONENT_IDENTIFICATION;
      }
      case 7: {
        return plc4c_s7_read_write_szl_sublist_INTERRUPT_STATUS;
      }
      case 8: {
        return plc4c_s7_read_write_szl_sublist_ASSIGNMENT_BETWEEN_PROCESS_IMAGE_PARTITIONS_AND_OBS;
      }
      case 9: {
        return plc4c_s7_read_write_szl_sublist_COMMUNICATION_STATUS_DATA;
      }
      case 10: {
        return plc4c_s7_read_write_szl_sublist_STATUS_SINGLE_MODULE_LED;
      }
      case 11: {
        return plc4c_s7_read_write_szl_sublist_DP_MASTER_SYSTEM_INFORMATION;
      }
      case 12: {
        return plc4c_s7_read_write_szl_sublist_MODULE_STATUS_INFORMATION;
      }
      case 13: {
        return plc4c_s7_read_write_szl_sublist_RACK_OR_STATION_STATUS_INFORMATION;
      }
      case 14: {
        return plc4c_s7_read_write_szl_sublist_RACK_OR_STATION_STATUS_INFORMATION_2;
      }
      case 15: {
        return plc4c_s7_read_write_szl_sublist_ADDITIONAL_DP_MASTER_SYSTEM_OR_PROFINET_IO_SYSTEM_INFORMATION;
      }
      case 16: {
        return plc4c_s7_read_write_szl_sublist_MODULE_STATUS_INFORMATION_PROFINET_IO_AND_PROFIBUS_DP;
      }
      case 17: {
        return plc4c_s7_read_write_szl_sublist_DIAGNOSTIC_BUFFER;
      }
      case 18: {
        return plc4c_s7_read_write_szl_sublist_MODULE_DIAGNOSTIC_DATA;
      }
      default: {
        return -1;
      }
    }
}
