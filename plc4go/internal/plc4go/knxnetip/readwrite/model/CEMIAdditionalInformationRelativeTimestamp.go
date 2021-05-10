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
	"fmt"
	"github.com/apache/plc4x/plc4go/internal/plc4go/spi/utils"
	"github.com/pkg/errors"
)

// Code generated by build-utils. DO NOT EDIT.

// Constant values.
const CEMIAdditionalInformationRelativeTimestamp_LEN uint8 = 2

// The data-structure of this message
type CEMIAdditionalInformationRelativeTimestamp struct {
	RelativeTimestamp *RelativeTimestamp
	Parent            *CEMIAdditionalInformation
}

// The corresponding interface
type ICEMIAdditionalInformationRelativeTimestamp interface {
	LengthInBytes() uint16
	LengthInBits() uint16
	Serialize(writeBuffer utils.WriteBuffer) error
}

///////////////////////////////////////////////////////////
// Accessors for discriminator values.
///////////////////////////////////////////////////////////
func (m *CEMIAdditionalInformationRelativeTimestamp) AdditionalInformationType() uint8 {
	return 0x04
}

func (m *CEMIAdditionalInformationRelativeTimestamp) InitializeParent(parent *CEMIAdditionalInformation) {
}

func NewCEMIAdditionalInformationRelativeTimestamp(relativeTimestamp *RelativeTimestamp) *CEMIAdditionalInformation {
	child := &CEMIAdditionalInformationRelativeTimestamp{
		RelativeTimestamp: relativeTimestamp,
		Parent:            NewCEMIAdditionalInformation(),
	}
	child.Parent.Child = child
	return child.Parent
}

func CastCEMIAdditionalInformationRelativeTimestamp(structType interface{}) *CEMIAdditionalInformationRelativeTimestamp {
	castFunc := func(typ interface{}) *CEMIAdditionalInformationRelativeTimestamp {
		if casted, ok := typ.(CEMIAdditionalInformationRelativeTimestamp); ok {
			return &casted
		}
		if casted, ok := typ.(*CEMIAdditionalInformationRelativeTimestamp); ok {
			return casted
		}
		if casted, ok := typ.(CEMIAdditionalInformation); ok {
			return CastCEMIAdditionalInformationRelativeTimestamp(casted.Child)
		}
		if casted, ok := typ.(*CEMIAdditionalInformation); ok {
			return CastCEMIAdditionalInformationRelativeTimestamp(casted.Child)
		}
		return nil
	}
	return castFunc(structType)
}

func (m *CEMIAdditionalInformationRelativeTimestamp) GetTypeName() string {
	return "CEMIAdditionalInformationRelativeTimestamp"
}

func (m *CEMIAdditionalInformationRelativeTimestamp) LengthInBits() uint16 {
	return m.LengthInBitsConditional(false)
}

func (m *CEMIAdditionalInformationRelativeTimestamp) LengthInBitsConditional(lastItem bool) uint16 {
	lengthInBits := uint16(m.Parent.ParentLengthInBits())

	// Const Field (len)
	lengthInBits += 8

	// Simple field (relativeTimestamp)
	lengthInBits += m.RelativeTimestamp.LengthInBits()

	return lengthInBits
}

func (m *CEMIAdditionalInformationRelativeTimestamp) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func CEMIAdditionalInformationRelativeTimestampParse(readBuffer utils.ReadBuffer) (*CEMIAdditionalInformation, error) {
	if pullErr := readBuffer.PullContext("CEMIAdditionalInformationRelativeTimestamp"); pullErr != nil {
		return nil, pullErr
	}

	// Const Field (len)
	len, _lenErr := readBuffer.ReadUint8("len", 8)
	if _lenErr != nil {
		return nil, errors.Wrap(_lenErr, "Error parsing 'len' field")
	}
	if len != CEMIAdditionalInformationRelativeTimestamp_LEN {
		return nil, errors.New("Expected constant value " + fmt.Sprintf("%d", CEMIAdditionalInformationRelativeTimestamp_LEN) + " but got " + fmt.Sprintf("%d", len))
	}

	if pullErr := readBuffer.PullContext("relativeTimestamp"); pullErr != nil {
		return nil, pullErr
	}

	// Simple Field (relativeTimestamp)
	relativeTimestamp, _relativeTimestampErr := RelativeTimestampParse(readBuffer)
	if _relativeTimestampErr != nil {
		return nil, errors.Wrap(_relativeTimestampErr, "Error parsing 'relativeTimestamp' field")
	}
	if closeErr := readBuffer.CloseContext("relativeTimestamp"); closeErr != nil {
		return nil, closeErr
	}

	if closeErr := readBuffer.CloseContext("CEMIAdditionalInformationRelativeTimestamp"); closeErr != nil {
		return nil, closeErr
	}

	// Create a partially initialized instance
	_child := &CEMIAdditionalInformationRelativeTimestamp{
		RelativeTimestamp: relativeTimestamp,
		Parent:            &CEMIAdditionalInformation{},
	}
	_child.Parent.Child = _child
	return _child.Parent, nil
}

func (m *CEMIAdditionalInformationRelativeTimestamp) Serialize(writeBuffer utils.WriteBuffer) error {
	ser := func() error {
		if pushErr := writeBuffer.PushContext("CEMIAdditionalInformationRelativeTimestamp"); pushErr != nil {
			return pushErr
		}

		// Const Field (len)
		_lenErr := writeBuffer.WriteUint8("len", 8, 2)
		if _lenErr != nil {
			return errors.Wrap(_lenErr, "Error serializing 'len' field")
		}

		// Simple Field (relativeTimestamp)
		if pushErr := writeBuffer.PushContext("relativeTimestamp"); pushErr != nil {
			return pushErr
		}
		_relativeTimestampErr := m.RelativeTimestamp.Serialize(writeBuffer)
		if popErr := writeBuffer.PopContext("relativeTimestamp"); popErr != nil {
			return popErr
		}
		if _relativeTimestampErr != nil {
			return errors.Wrap(_relativeTimestampErr, "Error serializing 'relativeTimestamp' field")
		}

		if popErr := writeBuffer.PopContext("CEMIAdditionalInformationRelativeTimestamp"); popErr != nil {
			return popErr
		}
		return nil
	}
	return m.Parent.SerializeParent(writeBuffer, m, ser)
}

func (m *CEMIAdditionalInformationRelativeTimestamp) String() string {
	if m == nil {
		return "<nil>"
	}
	buffer := utils.NewBoxedWriteBufferWithOptions(true, true)
	m.Serialize(buffer)
	return buffer.GetBox().String()
}
