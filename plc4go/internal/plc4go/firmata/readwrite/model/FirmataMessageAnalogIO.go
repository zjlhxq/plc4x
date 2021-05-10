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
type FirmataMessageAnalogIO struct {
	Pin    uint8
	Data   []int8
	Parent *FirmataMessage
}

// The corresponding interface
type IFirmataMessageAnalogIO interface {
	LengthInBytes() uint16
	LengthInBits() uint16
	Serialize(writeBuffer utils.WriteBuffer) error
}

///////////////////////////////////////////////////////////
// Accessors for discriminator values.
///////////////////////////////////////////////////////////
func (m *FirmataMessageAnalogIO) MessageType() uint8 {
	return 0xE
}

func (m *FirmataMessageAnalogIO) InitializeParent(parent *FirmataMessage) {
}

func NewFirmataMessageAnalogIO(pin uint8, data []int8) *FirmataMessage {
	child := &FirmataMessageAnalogIO{
		Pin:    pin,
		Data:   data,
		Parent: NewFirmataMessage(),
	}
	child.Parent.Child = child
	return child.Parent
}

func CastFirmataMessageAnalogIO(structType interface{}) *FirmataMessageAnalogIO {
	castFunc := func(typ interface{}) *FirmataMessageAnalogIO {
		if casted, ok := typ.(FirmataMessageAnalogIO); ok {
			return &casted
		}
		if casted, ok := typ.(*FirmataMessageAnalogIO); ok {
			return casted
		}
		if casted, ok := typ.(FirmataMessage); ok {
			return CastFirmataMessageAnalogIO(casted.Child)
		}
		if casted, ok := typ.(*FirmataMessage); ok {
			return CastFirmataMessageAnalogIO(casted.Child)
		}
		return nil
	}
	return castFunc(structType)
}

func (m *FirmataMessageAnalogIO) GetTypeName() string {
	return "FirmataMessageAnalogIO"
}

func (m *FirmataMessageAnalogIO) LengthInBits() uint16 {
	return m.LengthInBitsConditional(false)
}

func (m *FirmataMessageAnalogIO) LengthInBitsConditional(lastItem bool) uint16 {
	lengthInBits := uint16(m.Parent.ParentLengthInBits())

	// Simple field (pin)
	lengthInBits += 4

	// Array field
	if len(m.Data) > 0 {
		lengthInBits += 8 * uint16(len(m.Data))
	}

	return lengthInBits
}

func (m *FirmataMessageAnalogIO) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func FirmataMessageAnalogIOParse(readBuffer utils.ReadBuffer) (*FirmataMessage, error) {
	if pullErr := readBuffer.PullContext("FirmataMessageAnalogIO"); pullErr != nil {
		return nil, pullErr
	}

	// Simple Field (pin)
	pin, _pinErr := readBuffer.ReadUint8("pin", 4)
	if _pinErr != nil {
		return nil, errors.Wrap(_pinErr, "Error parsing 'pin' field")
	}

	// Array field (data)
	if pullErr := readBuffer.PullContext("data", utils.WithRenderAsList(true)); pullErr != nil {
		return nil, pullErr
	}
	// Count array
	data := make([]int8, uint16(2))
	for curItem := uint16(0); curItem < uint16(uint16(2)); curItem++ {
		_item, _err := readBuffer.ReadInt8("", 8)
		if _err != nil {
			return nil, errors.Wrap(_err, "Error parsing 'data' field")
		}
		data[curItem] = _item
	}
	if closeErr := readBuffer.CloseContext("data", utils.WithRenderAsList(true)); closeErr != nil {
		return nil, closeErr
	}

	if closeErr := readBuffer.CloseContext("FirmataMessageAnalogIO"); closeErr != nil {
		return nil, closeErr
	}

	// Create a partially initialized instance
	_child := &FirmataMessageAnalogIO{
		Pin:    pin,
		Data:   data,
		Parent: &FirmataMessage{},
	}
	_child.Parent.Child = _child
	return _child.Parent, nil
}

func (m *FirmataMessageAnalogIO) Serialize(writeBuffer utils.WriteBuffer) error {
	ser := func() error {
		if pushErr := writeBuffer.PushContext("FirmataMessageAnalogIO"); pushErr != nil {
			return pushErr
		}

		// Simple Field (pin)
		pin := uint8(m.Pin)
		_pinErr := writeBuffer.WriteUint8("pin", 4, (pin))
		if _pinErr != nil {
			return errors.Wrap(_pinErr, "Error serializing 'pin' field")
		}

		// Array Field (data)
		if m.Data != nil {
			if pushErr := writeBuffer.PushContext("data", utils.WithRenderAsList(true)); pushErr != nil {
				return pushErr
			}
			for _, _element := range m.Data {
				_elementErr := writeBuffer.WriteInt8("", 8, _element)
				if _elementErr != nil {
					return errors.Wrap(_elementErr, "Error serializing 'data' field")
				}
			}
			if popErr := writeBuffer.PopContext("data", utils.WithRenderAsList(true)); popErr != nil {
				return popErr
			}
		}

		if popErr := writeBuffer.PopContext("FirmataMessageAnalogIO"); popErr != nil {
			return popErr
		}
		return nil
	}
	return m.Parent.SerializeParent(writeBuffer, m, ser)
}

func (m *FirmataMessageAnalogIO) String() string {
	if m == nil {
		return "<nil>"
	}
	buffer := utils.NewBoxedWriteBufferWithOptions(true, true)
	m.Serialize(buffer)
	return buffer.GetBox().String()
}
