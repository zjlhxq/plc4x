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
	"github.com/apache/plc4x/plc4go/internal/plc4go/spi/utils"
	"github.com/pkg/errors"
)

// Code generated by build-utils. DO NOT EDIT.

// The data-structure of this message
type DescriptionResponse struct {
	DibDeviceInfo      *DIBDeviceInfo
	DibSuppSvcFamilies *DIBSuppSvcFamilies
	Parent             *KnxNetIpMessage
}

// The corresponding interface
type IDescriptionResponse interface {
	LengthInBytes() uint16
	LengthInBits() uint16
	Serialize(writeBuffer utils.WriteBuffer) error
}

///////////////////////////////////////////////////////////
// Accessors for discriminator values.
///////////////////////////////////////////////////////////
func (m *DescriptionResponse) MsgType() uint16 {
	return 0x0204
}

func (m *DescriptionResponse) InitializeParent(parent *KnxNetIpMessage) {
}

func NewDescriptionResponse(dibDeviceInfo *DIBDeviceInfo, dibSuppSvcFamilies *DIBSuppSvcFamilies) *KnxNetIpMessage {
	child := &DescriptionResponse{
		DibDeviceInfo:      dibDeviceInfo,
		DibSuppSvcFamilies: dibSuppSvcFamilies,
		Parent:             NewKnxNetIpMessage(),
	}
	child.Parent.Child = child
	return child.Parent
}

func CastDescriptionResponse(structType interface{}) *DescriptionResponse {
	castFunc := func(typ interface{}) *DescriptionResponse {
		if casted, ok := typ.(DescriptionResponse); ok {
			return &casted
		}
		if casted, ok := typ.(*DescriptionResponse); ok {
			return casted
		}
		if casted, ok := typ.(KnxNetIpMessage); ok {
			return CastDescriptionResponse(casted.Child)
		}
		if casted, ok := typ.(*KnxNetIpMessage); ok {
			return CastDescriptionResponse(casted.Child)
		}
		return nil
	}
	return castFunc(structType)
}

func (m *DescriptionResponse) GetTypeName() string {
	return "DescriptionResponse"
}

func (m *DescriptionResponse) LengthInBits() uint16 {
	return m.LengthInBitsConditional(false)
}

func (m *DescriptionResponse) LengthInBitsConditional(lastItem bool) uint16 {
	lengthInBits := uint16(m.Parent.ParentLengthInBits())

	// Simple field (dibDeviceInfo)
	lengthInBits += m.DibDeviceInfo.LengthInBits()

	// Simple field (dibSuppSvcFamilies)
	lengthInBits += m.DibSuppSvcFamilies.LengthInBits()

	return lengthInBits
}

func (m *DescriptionResponse) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func DescriptionResponseParse(readBuffer utils.ReadBuffer) (*KnxNetIpMessage, error) {
	if pullErr := readBuffer.PullContext("DescriptionResponse"); pullErr != nil {
		return nil, pullErr
	}

	if pullErr := readBuffer.PullContext("dibDeviceInfo"); pullErr != nil {
		return nil, pullErr
	}

	// Simple Field (dibDeviceInfo)
	dibDeviceInfo, _dibDeviceInfoErr := DIBDeviceInfoParse(readBuffer)
	if _dibDeviceInfoErr != nil {
		return nil, errors.Wrap(_dibDeviceInfoErr, "Error parsing 'dibDeviceInfo' field")
	}
	if closeErr := readBuffer.CloseContext("dibDeviceInfo"); closeErr != nil {
		return nil, closeErr
	}

	if pullErr := readBuffer.PullContext("dibSuppSvcFamilies"); pullErr != nil {
		return nil, pullErr
	}

	// Simple Field (dibSuppSvcFamilies)
	dibSuppSvcFamilies, _dibSuppSvcFamiliesErr := DIBSuppSvcFamiliesParse(readBuffer)
	if _dibSuppSvcFamiliesErr != nil {
		return nil, errors.Wrap(_dibSuppSvcFamiliesErr, "Error parsing 'dibSuppSvcFamilies' field")
	}
	if closeErr := readBuffer.CloseContext("dibSuppSvcFamilies"); closeErr != nil {
		return nil, closeErr
	}

	if closeErr := readBuffer.CloseContext("DescriptionResponse"); closeErr != nil {
		return nil, closeErr
	}

	// Create a partially initialized instance
	_child := &DescriptionResponse{
		DibDeviceInfo:      dibDeviceInfo,
		DibSuppSvcFamilies: dibSuppSvcFamilies,
		Parent:             &KnxNetIpMessage{},
	}
	_child.Parent.Child = _child
	return _child.Parent, nil
}

func (m *DescriptionResponse) Serialize(writeBuffer utils.WriteBuffer) error {
	ser := func() error {
		if pushErr := writeBuffer.PushContext("DescriptionResponse"); pushErr != nil {
			return pushErr
		}

		// Simple Field (dibDeviceInfo)
		if pushErr := writeBuffer.PushContext("dibDeviceInfo"); pushErr != nil {
			return pushErr
		}
		_dibDeviceInfoErr := m.DibDeviceInfo.Serialize(writeBuffer)
		if popErr := writeBuffer.PopContext("dibDeviceInfo"); popErr != nil {
			return popErr
		}
		if _dibDeviceInfoErr != nil {
			return errors.Wrap(_dibDeviceInfoErr, "Error serializing 'dibDeviceInfo' field")
		}

		// Simple Field (dibSuppSvcFamilies)
		if pushErr := writeBuffer.PushContext("dibSuppSvcFamilies"); pushErr != nil {
			return pushErr
		}
		_dibSuppSvcFamiliesErr := m.DibSuppSvcFamilies.Serialize(writeBuffer)
		if popErr := writeBuffer.PopContext("dibSuppSvcFamilies"); popErr != nil {
			return popErr
		}
		if _dibSuppSvcFamiliesErr != nil {
			return errors.Wrap(_dibSuppSvcFamiliesErr, "Error serializing 'dibSuppSvcFamilies' field")
		}

		if popErr := writeBuffer.PopContext("DescriptionResponse"); popErr != nil {
			return popErr
		}
		return nil
	}
	return m.Parent.SerializeParent(writeBuffer, m, ser)
}

func (m *DescriptionResponse) String() string {
	if m == nil {
		return "<nil>"
	}
	buffer := utils.NewBoxedWriteBufferWithOptions(true, true)
	m.Serialize(buffer)
	return buffer.GetBox().String()
}
