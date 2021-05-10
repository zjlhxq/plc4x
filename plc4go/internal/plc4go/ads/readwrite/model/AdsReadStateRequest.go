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
type AdsReadStateRequest struct {
	Parent *AdsData
}

// The corresponding interface
type IAdsReadStateRequest interface {
	LengthInBytes() uint16
	LengthInBits() uint16
	Serialize(writeBuffer utils.WriteBuffer) error
}

///////////////////////////////////////////////////////////
// Accessors for discriminator values.
///////////////////////////////////////////////////////////
func (m *AdsReadStateRequest) CommandId() CommandId {
	return CommandId_ADS_READ_STATE
}

func (m *AdsReadStateRequest) Response() bool {
	return false
}

func (m *AdsReadStateRequest) InitializeParent(parent *AdsData) {
}

func NewAdsReadStateRequest() *AdsData {
	child := &AdsReadStateRequest{
		Parent: NewAdsData(),
	}
	child.Parent.Child = child
	return child.Parent
}

func CastAdsReadStateRequest(structType interface{}) *AdsReadStateRequest {
	castFunc := func(typ interface{}) *AdsReadStateRequest {
		if casted, ok := typ.(AdsReadStateRequest); ok {
			return &casted
		}
		if casted, ok := typ.(*AdsReadStateRequest); ok {
			return casted
		}
		if casted, ok := typ.(AdsData); ok {
			return CastAdsReadStateRequest(casted.Child)
		}
		if casted, ok := typ.(*AdsData); ok {
			return CastAdsReadStateRequest(casted.Child)
		}
		return nil
	}
	return castFunc(structType)
}

func (m *AdsReadStateRequest) GetTypeName() string {
	return "AdsReadStateRequest"
}

func (m *AdsReadStateRequest) LengthInBits() uint16 {
	return m.LengthInBitsConditional(false)
}

func (m *AdsReadStateRequest) LengthInBitsConditional(lastItem bool) uint16 {
	lengthInBits := uint16(m.Parent.ParentLengthInBits())

	return lengthInBits
}

func (m *AdsReadStateRequest) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func AdsReadStateRequestParse(readBuffer utils.ReadBuffer) (*AdsData, error) {
	if pullErr := readBuffer.PullContext("AdsReadStateRequest"); pullErr != nil {
		return nil, pullErr
	}

	if closeErr := readBuffer.CloseContext("AdsReadStateRequest"); closeErr != nil {
		return nil, closeErr
	}

	// Create a partially initialized instance
	_child := &AdsReadStateRequest{
		Parent: &AdsData{},
	}
	_child.Parent.Child = _child
	return _child.Parent, nil
}

func (m *AdsReadStateRequest) Serialize(writeBuffer utils.WriteBuffer) error {
	ser := func() error {
		if pushErr := writeBuffer.PushContext("AdsReadStateRequest"); pushErr != nil {
			return pushErr
		}

		if popErr := writeBuffer.PopContext("AdsReadStateRequest"); popErr != nil {
			return popErr
		}
		return nil
	}
	return m.Parent.SerializeParent(writeBuffer, m, ser)
}

func (m *AdsReadStateRequest) String() string {
	if m == nil {
		return "<nil>"
	}
	buffer := utils.NewBoxedWriteBufferWithOptions(true, true)
	m.Serialize(buffer)
	return buffer.GetBox().String()
}
