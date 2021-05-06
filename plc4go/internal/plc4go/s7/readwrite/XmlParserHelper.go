//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package readwrite

import (
	"github.com/apache/plc4x/plc4go/internal/plc4go/s7/readwrite/model"
	"github.com/apache/plc4x/plc4go/internal/plc4go/spi/utils"
	"github.com/pkg/errors"
	"strconv"
	"strings"
)

// Code generated by build-utils. DO NOT EDIT.

type S7XmlParserHelper struct {
}

// Temporary imports to silent compiler warnings (TODO: migrate from static to emission based imports)
func init() {
	_ = strconv.Atoi
	_ = strings.Join
	_ = utils.Dump
}

func (m S7XmlParserHelper) Parse(typeName string, xmlString string, parserArguments ...string) (interface{}, error) {
	switch typeName {
	case "SzlId":
		return model.SzlIdParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)))
	case "S7Message":
		return model.S7MessageParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)))
	case "S7VarPayloadStatusItem":
		return model.S7VarPayloadStatusItemParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)))
	case "S7Parameter":
		atoi, err := strconv.Atoi(parserArguments[0])
		if err != nil {
			return nil, err
		}
		messageType := uint8(atoi)
		return model.S7ParameterParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)), messageType)
	case "SzlDataTreeItem":
		return model.SzlDataTreeItemParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)))
	case "COTPPacket":
		atoi, err := strconv.Atoi(parserArguments[0])
		if err != nil {
			return nil, err
		}
		cotpLen := uint16(atoi)
		return model.COTPPacketParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)), cotpLen)
	case "S7PayloadUserDataItem":
		atoi, err := strconv.Atoi(parserArguments[0])
		if err != nil {
			return nil, err
		}
		cpuFunctionType := uint8(atoi)
		return model.S7PayloadUserDataItemParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)), cpuFunctionType)
	case "COTPParameter":
		atoi, err := strconv.Atoi(parserArguments[0])
		if err != nil {
			return nil, err
		}
		rest := uint8(atoi)
		return model.COTPParameterParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)), rest)
	case "TPKTPacket":
		return model.TPKTPacketParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)))
	case "S7Payload":
		atoi, err := strconv.Atoi(parserArguments[0])
		if err != nil {
			return nil, err
		}
		messageType := uint8(atoi)
		// TODO: find a way to parse the sub types
		var parameter model.S7Parameter
		return model.S7PayloadParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)), messageType, &parameter)
	case "S7VarRequestParameterItem":
		return model.S7VarRequestParameterItemParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)))
	case "S7VarPayloadDataItem":
		lastItem := parserArguments[0] == "true"
		return model.S7VarPayloadDataItemParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)), lastItem)
	case "S7Address":
		return model.S7AddressParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)))
	case "S7ParameterUserDataItem":
		return model.S7ParameterUserDataItemParse(utils.NewXmlReadBuffer(strings.NewReader(xmlString)))
	}
	return nil, errors.Errorf("Unsupported type %s", typeName)
}
