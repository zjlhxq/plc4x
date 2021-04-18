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

package model

import (
	"encoding/xml"
	"fmt"
	"github.com/apache/plc4x/plc4go/internal/plc4go/spi/utils"
	"io"
)

// Code generated by build-utils. DO NOT EDIT.

type SzlModuleTypeClass uint8

type ISzlModuleTypeClass interface {
	Serialize(io utils.WriteBuffer) error
	xml.Marshaler
	xml.Unmarshaler
}

const (
	SzlModuleTypeClass_CPU SzlModuleTypeClass = 0x0
	SzlModuleTypeClass_IM  SzlModuleTypeClass = 0x4
	SzlModuleTypeClass_FM  SzlModuleTypeClass = 0x8
	SzlModuleTypeClass_CP  SzlModuleTypeClass = 0xC
)

var SzlModuleTypeClassValues []SzlModuleTypeClass

func init() {
	SzlModuleTypeClassValues = []SzlModuleTypeClass{
		SzlModuleTypeClass_CPU,
		SzlModuleTypeClass_IM,
		SzlModuleTypeClass_FM,
		SzlModuleTypeClass_CP,
	}
}

func SzlModuleTypeClassByValue(value uint8) SzlModuleTypeClass {
	switch value {
	case 0x0:
		return SzlModuleTypeClass_CPU
	case 0x4:
		return SzlModuleTypeClass_IM
	case 0x8:
		return SzlModuleTypeClass_FM
	case 0xC:
		return SzlModuleTypeClass_CP
	}
	return 0
}

func SzlModuleTypeClassByName(value string) SzlModuleTypeClass {
	switch value {
	case "CPU":
		return SzlModuleTypeClass_CPU
	case "IM":
		return SzlModuleTypeClass_IM
	case "FM":
		return SzlModuleTypeClass_FM
	case "CP":
		return SzlModuleTypeClass_CP
	}
	return 0
}

func CastSzlModuleTypeClass(structType interface{}) SzlModuleTypeClass {
	castFunc := func(typ interface{}) SzlModuleTypeClass {
		if sSzlModuleTypeClass, ok := typ.(SzlModuleTypeClass); ok {
			return sSzlModuleTypeClass
		}
		return 0
	}
	return castFunc(structType)
}

func (m SzlModuleTypeClass) LengthInBits() uint16 {
	return 4
}

func (m SzlModuleTypeClass) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func SzlModuleTypeClassParse(io utils.ReadBuffer) (SzlModuleTypeClass, error) {
	val, err := io.ReadUint8(4)
	if err != nil {
		return 0, nil
	}
	return SzlModuleTypeClassByValue(val), nil
}

func (e SzlModuleTypeClass) Serialize(io utils.WriteBuffer) error {
	err := io.WriteUint8("SzlModuleTypeClass", 4, uint8(e))
	return err
}

func (m *SzlModuleTypeClass) UnmarshalXML(d *xml.Decoder, start xml.StartElement) error {
	var token xml.Token
	var err error
	for {
		token, err = d.Token()
		if err != nil {
			if err == io.EOF {
				return nil
			}
			return err
		}
		switch token.(type) {
		case xml.CharData:
			tok := token.(xml.CharData)
			*m = SzlModuleTypeClassByName(string(tok))
		}
	}
}

func (m SzlModuleTypeClass) MarshalXML(e *xml.Encoder, start xml.StartElement) error {
	if err := e.EncodeElement(m.String(), start); err != nil {
		return err
	}
	return nil
}

func (e SzlModuleTypeClass) name() string {
	switch e {
	case SzlModuleTypeClass_CPU:
		return "CPU"
	case SzlModuleTypeClass_IM:
		return "IM"
	case SzlModuleTypeClass_FM:
		return "FM"
	case SzlModuleTypeClass_CP:
		return "CP"
	}
	return ""
}

func (e SzlModuleTypeClass) String() string {
	return e.name()
}

func (m SzlModuleTypeClass) Box(s string, i int) utils.AsciiBox {
	boxName := "SzlModuleTypeClass"
	if s != "" {
		boxName += "/" + s
	}
	return utils.BoxString(boxName, fmt.Sprintf("%#0*x %s", 1, uint8(m), m.name()), -1)
}
