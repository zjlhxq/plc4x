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
)

// Code generated by build-utils. DO NOT EDIT.

// The data-structure of this message
type BACnetErrorGetEnrollmentSummary struct {
	Parent *BACnetError
}

// The corresponding interface
type IBACnetErrorGetEnrollmentSummary interface {
	LengthInBytes() uint16
	LengthInBits() uint16
	Serialize(writeBuffer utils.WriteBuffer) error
}

///////////////////////////////////////////////////////////
// Accessors for discriminator values.
///////////////////////////////////////////////////////////
func (m *BACnetErrorGetEnrollmentSummary) ServiceChoice() uint8 {
	return 0x04
}

func (m *BACnetErrorGetEnrollmentSummary) InitializeParent(parent *BACnetError) {
}

func NewBACnetErrorGetEnrollmentSummary() *BACnetError {
	child := &BACnetErrorGetEnrollmentSummary{
		Parent: NewBACnetError(),
	}
	child.Parent.Child = child
	return child.Parent
}

func CastBACnetErrorGetEnrollmentSummary(structType interface{}) *BACnetErrorGetEnrollmentSummary {
	castFunc := func(typ interface{}) *BACnetErrorGetEnrollmentSummary {
		if casted, ok := typ.(BACnetErrorGetEnrollmentSummary); ok {
			return &casted
		}
		if casted, ok := typ.(*BACnetErrorGetEnrollmentSummary); ok {
			return casted
		}
		if casted, ok := typ.(BACnetError); ok {
			return CastBACnetErrorGetEnrollmentSummary(casted.Child)
		}
		if casted, ok := typ.(*BACnetError); ok {
			return CastBACnetErrorGetEnrollmentSummary(casted.Child)
		}
		return nil
	}
	return castFunc(structType)
}

func (m *BACnetErrorGetEnrollmentSummary) GetTypeName() string {
	return "BACnetErrorGetEnrollmentSummary"
}

func (m *BACnetErrorGetEnrollmentSummary) LengthInBits() uint16 {
	return m.LengthInBitsConditional(false)
}

func (m *BACnetErrorGetEnrollmentSummary) LengthInBitsConditional(lastItem bool) uint16 {
	lengthInBits := uint16(m.Parent.ParentLengthInBits())

	return lengthInBits
}

func (m *BACnetErrorGetEnrollmentSummary) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func BACnetErrorGetEnrollmentSummaryParse(readBuffer utils.ReadBuffer) (*BACnetError, error) {
	if pullErr := readBuffer.PullContext("BACnetErrorGetEnrollmentSummary"); pullErr != nil {
		return nil, pullErr
	}

	if closeErr := readBuffer.CloseContext("BACnetErrorGetEnrollmentSummary"); closeErr != nil {
		return nil, closeErr
	}

	// Create a partially initialized instance
	_child := &BACnetErrorGetEnrollmentSummary{
		Parent: &BACnetError{},
	}
	_child.Parent.Child = _child
	return _child.Parent, nil
}

func (m *BACnetErrorGetEnrollmentSummary) Serialize(writeBuffer utils.WriteBuffer) error {
	ser := func() error {
		if pushErr := writeBuffer.PushContext("BACnetErrorGetEnrollmentSummary"); pushErr != nil {
			return pushErr
		}

		if popErr := writeBuffer.PopContext("BACnetErrorGetEnrollmentSummary"); popErr != nil {
			return popErr
		}
		return nil
	}
	return m.Parent.SerializeParent(writeBuffer, m, ser)
}

func (m *BACnetErrorGetEnrollmentSummary) String() string {
	if m == nil {
		return "<nil>"
	}
	buffer := utils.NewBoxedWriteBufferWithOptions(true, true)
	m.Serialize(buffer)
	return buffer.GetBox().String()
}
