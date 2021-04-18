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
	"github.com/apache/plc4x/plc4go/internal/plc4go/spi/utils"
	"github.com/pkg/errors"
	"io"
)

// Code generated by build-utils. DO NOT EDIT.

// The data-structure of this message
type HPAIDataEndpoint struct {
	HostProtocolCode HostProtocolCode
	IpAddress        *IPAddress
	IpPort           uint16
}

// The corresponding interface
type IHPAIDataEndpoint interface {
	LengthInBytes() uint16
	LengthInBits() uint16
	Serialize(io utils.WriteBuffer) error
	xml.Marshaler
	xml.Unmarshaler
}

func NewHPAIDataEndpoint(hostProtocolCode HostProtocolCode, ipAddress *IPAddress, ipPort uint16) *HPAIDataEndpoint {
	return &HPAIDataEndpoint{HostProtocolCode: hostProtocolCode, IpAddress: ipAddress, IpPort: ipPort}
}

func CastHPAIDataEndpoint(structType interface{}) *HPAIDataEndpoint {
	castFunc := func(typ interface{}) *HPAIDataEndpoint {
		if casted, ok := typ.(HPAIDataEndpoint); ok {
			return &casted
		}
		if casted, ok := typ.(*HPAIDataEndpoint); ok {
			return casted
		}
		return nil
	}
	return castFunc(structType)
}

func (m *HPAIDataEndpoint) GetTypeName() string {
	return "HPAIDataEndpoint"
}

func (m *HPAIDataEndpoint) LengthInBits() uint16 {
	return m.LengthInBitsConditional(false)
}

func (m *HPAIDataEndpoint) LengthInBitsConditional(lastItem bool) uint16 {
	lengthInBits := uint16(0)

	// Implicit Field (structureLength)
	lengthInBits += 8

	// Simple field (hostProtocolCode)
	lengthInBits += 8

	// Simple field (ipAddress)
	lengthInBits += m.IpAddress.LengthInBits()

	// Simple field (ipPort)
	lengthInBits += 16

	return lengthInBits
}

func (m *HPAIDataEndpoint) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func HPAIDataEndpointParse(io utils.ReadBuffer) (*HPAIDataEndpoint, error) {

	// Implicit Field (structureLength) (Used for parsing, but it's value is not stored as it's implicitly given by the objects content)
	structureLength, _structureLengthErr := io.ReadUint8(8)
	_ = structureLength
	if _structureLengthErr != nil {
		return nil, errors.Wrap(_structureLengthErr, "Error parsing 'structureLength' field")
	}

	// Simple Field (hostProtocolCode)
	hostProtocolCode, _hostProtocolCodeErr := HostProtocolCodeParse(io)
	if _hostProtocolCodeErr != nil {
		return nil, errors.Wrap(_hostProtocolCodeErr, "Error parsing 'hostProtocolCode' field")
	}

	// Simple Field (ipAddress)
	ipAddress, _ipAddressErr := IPAddressParse(io)
	if _ipAddressErr != nil {
		return nil, errors.Wrap(_ipAddressErr, "Error parsing 'ipAddress' field")
	}

	// Simple Field (ipPort)
	ipPort, _ipPortErr := io.ReadUint16(16)
	if _ipPortErr != nil {
		return nil, errors.Wrap(_ipPortErr, "Error parsing 'ipPort' field")
	}

	// Create the instance
	return NewHPAIDataEndpoint(hostProtocolCode, ipAddress, ipPort), nil
}

func (m *HPAIDataEndpoint) Serialize(io utils.WriteBuffer) error {

	// Implicit Field (structureLength) (Used for parsing, but it's value is not stored as it's implicitly given by the objects content)
	structureLength := uint8(uint8(m.LengthInBytes()))
	_structureLengthErr := io.WriteUint8("structureLength", 8, (structureLength))
	if _structureLengthErr != nil {
		return errors.Wrap(_structureLengthErr, "Error serializing 'structureLength' field")
	}

	// Simple Field (hostProtocolCode)
	_hostProtocolCodeErr := m.HostProtocolCode.Serialize(io)
	if _hostProtocolCodeErr != nil {
		return errors.Wrap(_hostProtocolCodeErr, "Error serializing 'hostProtocolCode' field")
	}

	// Simple Field (ipAddress)
	_ipAddressErr := m.IpAddress.Serialize(io)
	if _ipAddressErr != nil {
		return errors.Wrap(_ipAddressErr, "Error serializing 'ipAddress' field")
	}

	// Simple Field (ipPort)
	ipPort := uint16(m.IpPort)
	_ipPortErr := io.WriteUint16("ipPort", 16, (ipPort))
	if _ipPortErr != nil {
		return errors.Wrap(_ipPortErr, "Error serializing 'ipPort' field")
	}

	return nil
}

func (m *HPAIDataEndpoint) UnmarshalXML(d *xml.Decoder, start xml.StartElement) error {
	var token xml.Token
	var err error
	foundContent := false
	for {
		token, err = d.Token()
		if err != nil {
			if err == io.EOF && foundContent {
				return nil
			}
			return err
		}
		switch token.(type) {
		case xml.StartElement:
			foundContent = true
			tok := token.(xml.StartElement)
			switch tok.Name.Local {
			case "hostProtocolCode":
				var data HostProtocolCode
				if err := d.DecodeElement(&data, &tok); err != nil {
					return err
				}
				m.HostProtocolCode = data
			case "ipAddress":
				var data IPAddress
				if err := d.DecodeElement(&data, &tok); err != nil {
					return err
				}
				m.IpAddress = &data
			case "ipPort":
				var data uint16
				if err := d.DecodeElement(&data, &tok); err != nil {
					return err
				}
				m.IpPort = data
			}
		}
	}
}

func (m *HPAIDataEndpoint) MarshalXML(e *xml.Encoder, start xml.StartElement) error {
	className := "org.apache.plc4x.java.knxnetip.readwrite.HPAIDataEndpoint"
	if err := e.EncodeToken(xml.StartElement{Name: start.Name, Attr: []xml.Attr{
		{Name: xml.Name{Local: "className"}, Value: className},
	}}); err != nil {
		return err
	}
	if err := e.EncodeElement(m.HostProtocolCode, xml.StartElement{Name: xml.Name{Local: "hostProtocolCode"}}); err != nil {
		return err
	}
	if err := e.EncodeElement(m.IpAddress, xml.StartElement{Name: xml.Name{Local: "ipAddress"}}); err != nil {
		return err
	}
	if err := e.EncodeElement(m.IpPort, xml.StartElement{Name: xml.Name{Local: "ipPort"}}); err != nil {
		return err
	}
	if err := e.EncodeToken(xml.EndElement{Name: start.Name}); err != nil {
		return err
	}
	return nil
}

func (m HPAIDataEndpoint) String() string {
	return string(m.Box("", 120))
}

func (m HPAIDataEndpoint) Box(name string, width int) utils.AsciiBox {
	boxName := "HPAIDataEndpoint"
	if name != "" {
		boxName += "/" + name
	}
	boxes := make([]utils.AsciiBox, 0)
	// Implicit Field (structureLength)
	structureLength := uint8(uint8(m.LengthInBytes()))
	// uint8 can be boxed as anything with the least amount of space
	boxes = append(boxes, utils.BoxAnything("StructureLength", structureLength, -1))
	// Complex field (case complex)
	boxes = append(boxes, m.HostProtocolCode.Box("hostProtocolCode", width-2))
	// Complex field (case complex)
	boxes = append(boxes, m.IpAddress.Box("ipAddress", width-2))
	// Simple field (case simple)
	// uint16 can be boxed as anything with the least amount of space
	boxes = append(boxes, utils.BoxAnything("IpPort", m.IpPort, -1))
	return utils.BoxBox(boxName, utils.AlignBoxes(boxes, width-2), 0)
}
