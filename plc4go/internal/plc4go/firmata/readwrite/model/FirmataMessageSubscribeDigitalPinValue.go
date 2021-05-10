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
	"github.com/rs/zerolog/log"
)

// Code generated by build-utils. DO NOT EDIT.

// The data-structure of this message
type FirmataMessageSubscribeDigitalPinValue struct {
	Pin    uint8
	Enable bool
	Parent *FirmataMessage
}

// The corresponding interface
type IFirmataMessageSubscribeDigitalPinValue interface {
	LengthInBytes() uint16
	LengthInBits() uint16
	Serialize(writeBuffer utils.WriteBuffer) error
}

///////////////////////////////////////////////////////////
// Accessors for discriminator values.
///////////////////////////////////////////////////////////
func (m *FirmataMessageSubscribeDigitalPinValue) MessageType() uint8 {
	return 0xD
}

func (m *FirmataMessageSubscribeDigitalPinValue) InitializeParent(parent *FirmataMessage) {
}

func NewFirmataMessageSubscribeDigitalPinValue(pin uint8, enable bool) *FirmataMessage {
	child := &FirmataMessageSubscribeDigitalPinValue{
		Pin:    pin,
		Enable: enable,
		Parent: NewFirmataMessage(),
	}
	child.Parent.Child = child
	return child.Parent
}

func CastFirmataMessageSubscribeDigitalPinValue(structType interface{}) *FirmataMessageSubscribeDigitalPinValue {
	castFunc := func(typ interface{}) *FirmataMessageSubscribeDigitalPinValue {
		if casted, ok := typ.(FirmataMessageSubscribeDigitalPinValue); ok {
			return &casted
		}
		if casted, ok := typ.(*FirmataMessageSubscribeDigitalPinValue); ok {
			return casted
		}
		if casted, ok := typ.(FirmataMessage); ok {
			return CastFirmataMessageSubscribeDigitalPinValue(casted.Child)
		}
		if casted, ok := typ.(*FirmataMessage); ok {
			return CastFirmataMessageSubscribeDigitalPinValue(casted.Child)
		}
		return nil
	}
	return castFunc(structType)
}

func (m *FirmataMessageSubscribeDigitalPinValue) GetTypeName() string {
	return "FirmataMessageSubscribeDigitalPinValue"
}

func (m *FirmataMessageSubscribeDigitalPinValue) LengthInBits() uint16 {
	return m.LengthInBitsConditional(false)
}

func (m *FirmataMessageSubscribeDigitalPinValue) LengthInBitsConditional(lastItem bool) uint16 {
	lengthInBits := uint16(m.Parent.ParentLengthInBits())

	// Simple field (pin)
	lengthInBits += 4

	// Reserved Field (reserved)
	lengthInBits += 7

	// Simple field (enable)
	lengthInBits += 1

	return lengthInBits
}

func (m *FirmataMessageSubscribeDigitalPinValue) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func FirmataMessageSubscribeDigitalPinValueParse(readBuffer utils.ReadBuffer) (*FirmataMessage, error) {
	if pullErr := readBuffer.PullContext("FirmataMessageSubscribeDigitalPinValue"); pullErr != nil {
		return nil, pullErr
	}

	// Simple Field (pin)
	pin, _pinErr := readBuffer.ReadUint8("pin", 4)
	if _pinErr != nil {
		return nil, errors.Wrap(_pinErr, "Error parsing 'pin' field")
	}

	// Reserved Field (Compartmentalized so the "reserved" variable can't leak)
	{
		reserved, _err := readBuffer.ReadUint8("reserved", 7)
		if _err != nil {
			return nil, errors.Wrap(_err, "Error parsing 'reserved' field")
		}
		if reserved != uint8(0x00) {
			log.Info().Fields(map[string]interface{}{
				"expected value": uint8(0x00),
				"got value":      reserved,
			}).Msg("Got unexpected response.")
		}
	}

	// Simple Field (enable)
	enable, _enableErr := readBuffer.ReadBit("enable")
	if _enableErr != nil {
		return nil, errors.Wrap(_enableErr, "Error parsing 'enable' field")
	}

	if closeErr := readBuffer.CloseContext("FirmataMessageSubscribeDigitalPinValue"); closeErr != nil {
		return nil, closeErr
	}

	// Create a partially initialized instance
	_child := &FirmataMessageSubscribeDigitalPinValue{
		Pin:    pin,
		Enable: enable,
		Parent: &FirmataMessage{},
	}
	_child.Parent.Child = _child
	return _child.Parent, nil
}

func (m *FirmataMessageSubscribeDigitalPinValue) Serialize(writeBuffer utils.WriteBuffer) error {
	ser := func() error {
		if pushErr := writeBuffer.PushContext("FirmataMessageSubscribeDigitalPinValue"); pushErr != nil {
			return pushErr
		}

		// Simple Field (pin)
		pin := uint8(m.Pin)
		_pinErr := writeBuffer.WriteUint8("pin", 4, (pin))
		if _pinErr != nil {
			return errors.Wrap(_pinErr, "Error serializing 'pin' field")
		}

		// Reserved Field (reserved)
		{
			_err := writeBuffer.WriteUint8("reserved", 7, uint8(0x00))
			if _err != nil {
				return errors.Wrap(_err, "Error serializing 'reserved' field")
			}
		}

		// Simple Field (enable)
		enable := bool(m.Enable)
		_enableErr := writeBuffer.WriteBit("enable", (enable))
		if _enableErr != nil {
			return errors.Wrap(_enableErr, "Error serializing 'enable' field")
		}

		if popErr := writeBuffer.PopContext("FirmataMessageSubscribeDigitalPinValue"); popErr != nil {
			return popErr
		}
		return nil
	}
	return m.Parent.SerializeParent(writeBuffer, m, ser)
}

func (m *FirmataMessageSubscribeDigitalPinValue) String() string {
	if m == nil {
		return "<nil>"
	}
	buffer := utils.NewBoxedWriteBufferWithOptions(true, true)
	m.Serialize(buffer)
	return buffer.GetBox().String()
}
