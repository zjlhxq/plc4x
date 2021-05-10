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
type APDU struct {
	Child IAPDUChild
}

// The corresponding interface
type IAPDU interface {
	ApduType() uint8
	LengthInBytes() uint16
	LengthInBits() uint16
	Serialize(writeBuffer utils.WriteBuffer) error
}

type IAPDUParent interface {
	SerializeParent(writeBuffer utils.WriteBuffer, child IAPDU, serializeChildFunction func() error) error
	GetTypeName() string
}

type IAPDUChild interface {
	Serialize(writeBuffer utils.WriteBuffer) error
	InitializeParent(parent *APDU)
	GetTypeName() string
	IAPDU
}

func NewAPDU() *APDU {
	return &APDU{}
}

func CastAPDU(structType interface{}) *APDU {
	castFunc := func(typ interface{}) *APDU {
		if casted, ok := typ.(APDU); ok {
			return &casted
		}
		if casted, ok := typ.(*APDU); ok {
			return casted
		}
		return nil
	}
	return castFunc(structType)
}

func (m *APDU) GetTypeName() string {
	return "APDU"
}

func (m *APDU) LengthInBits() uint16 {
	return m.LengthInBitsConditional(false)
}

func (m *APDU) LengthInBitsConditional(lastItem bool) uint16 {
	return m.Child.LengthInBits()
}

func (m *APDU) ParentLengthInBits() uint16 {
	lengthInBits := uint16(0)
	// Discriminator Field (apduType)
	lengthInBits += 4

	return lengthInBits
}

func (m *APDU) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func APDUParse(readBuffer utils.ReadBuffer, apduLength uint16) (*APDU, error) {
	if pullErr := readBuffer.PullContext("APDU"); pullErr != nil {
		return nil, pullErr
	}

	// Discriminator Field (apduType) (Used as input to a switch field)
	apduType, _apduTypeErr := readBuffer.ReadUint8("apduType", 4)
	if _apduTypeErr != nil {
		return nil, errors.Wrap(_apduTypeErr, "Error parsing 'apduType' field")
	}

	// Switch Field (Depending on the discriminator values, passes the instantiation to a sub-type)
	var _parent *APDU
	var typeSwitchError error
	switch {
	case apduType == 0x0: // APDUConfirmedRequest
		_parent, typeSwitchError = APDUConfirmedRequestParse(readBuffer, apduLength)
	case apduType == 0x1: // APDUUnconfirmedRequest
		_parent, typeSwitchError = APDUUnconfirmedRequestParse(readBuffer, apduLength)
	case apduType == 0x2: // APDUSimpleAck
		_parent, typeSwitchError = APDUSimpleAckParse(readBuffer)
	case apduType == 0x3: // APDUComplexAck
		_parent, typeSwitchError = APDUComplexAckParse(readBuffer)
	case apduType == 0x4: // APDUSegmentAck
		_parent, typeSwitchError = APDUSegmentAckParse(readBuffer)
	case apduType == 0x5: // APDUError
		_parent, typeSwitchError = APDUErrorParse(readBuffer)
	case apduType == 0x6: // APDUReject
		_parent, typeSwitchError = APDURejectParse(readBuffer)
	case apduType == 0x7: // APDUAbort
		_parent, typeSwitchError = APDUAbortParse(readBuffer)
	default:
		// TODO: return actual type
		typeSwitchError = errors.New("Unmapped type")
	}
	if typeSwitchError != nil {
		return nil, errors.Wrap(typeSwitchError, "Error parsing sub-type for type-switch.")
	}

	if closeErr := readBuffer.CloseContext("APDU"); closeErr != nil {
		return nil, closeErr
	}

	// Finish initializing
	_parent.Child.InitializeParent(_parent)
	return _parent, nil
}

func (m *APDU) Serialize(writeBuffer utils.WriteBuffer) error {
	return m.Child.Serialize(writeBuffer)
}

func (m *APDU) SerializeParent(writeBuffer utils.WriteBuffer, child IAPDU, serializeChildFunction func() error) error {
	if pushErr := writeBuffer.PushContext("APDU"); pushErr != nil {
		return pushErr
	}

	// Discriminator Field (apduType) (Used as input to a switch field)
	apduType := uint8(child.ApduType())
	_apduTypeErr := writeBuffer.WriteUint8("apduType", 4, (apduType))

	if _apduTypeErr != nil {
		return errors.Wrap(_apduTypeErr, "Error serializing 'apduType' field")
	}

	// Switch field (Depending on the discriminator values, passes the serialization to a sub-type)
	_typeSwitchErr := serializeChildFunction()
	if _typeSwitchErr != nil {
		return errors.Wrap(_typeSwitchErr, "Error serializing sub-type field")
	}

	if popErr := writeBuffer.PopContext("APDU"); popErr != nil {
		return popErr
	}
	return nil
}
