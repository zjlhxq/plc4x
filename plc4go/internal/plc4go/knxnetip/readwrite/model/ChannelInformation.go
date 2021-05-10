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
type ChannelInformation struct {
	NumChannels uint8
	ChannelCode uint16
}

// The corresponding interface
type IChannelInformation interface {
	LengthInBytes() uint16
	LengthInBits() uint16
	Serialize(writeBuffer utils.WriteBuffer) error
}

func NewChannelInformation(numChannels uint8, channelCode uint16) *ChannelInformation {
	return &ChannelInformation{NumChannels: numChannels, ChannelCode: channelCode}
}

func CastChannelInformation(structType interface{}) *ChannelInformation {
	castFunc := func(typ interface{}) *ChannelInformation {
		if casted, ok := typ.(ChannelInformation); ok {
			return &casted
		}
		if casted, ok := typ.(*ChannelInformation); ok {
			return casted
		}
		return nil
	}
	return castFunc(structType)
}

func (m *ChannelInformation) GetTypeName() string {
	return "ChannelInformation"
}

func (m *ChannelInformation) LengthInBits() uint16 {
	return m.LengthInBitsConditional(false)
}

func (m *ChannelInformation) LengthInBitsConditional(lastItem bool) uint16 {
	lengthInBits := uint16(0)

	// Simple field (numChannels)
	lengthInBits += 3

	// Simple field (channelCode)
	lengthInBits += 13

	return lengthInBits
}

func (m *ChannelInformation) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func ChannelInformationParse(readBuffer utils.ReadBuffer) (*ChannelInformation, error) {
	if pullErr := readBuffer.PullContext("ChannelInformation"); pullErr != nil {
		return nil, pullErr
	}

	// Simple Field (numChannels)
	numChannels, _numChannelsErr := readBuffer.ReadUint8("numChannels", 3)
	if _numChannelsErr != nil {
		return nil, errors.Wrap(_numChannelsErr, "Error parsing 'numChannels' field")
	}

	// Simple Field (channelCode)
	channelCode, _channelCodeErr := readBuffer.ReadUint16("channelCode", 13)
	if _channelCodeErr != nil {
		return nil, errors.Wrap(_channelCodeErr, "Error parsing 'channelCode' field")
	}

	if closeErr := readBuffer.CloseContext("ChannelInformation"); closeErr != nil {
		return nil, closeErr
	}

	// Create the instance
	return NewChannelInformation(numChannels, channelCode), nil
}

func (m *ChannelInformation) Serialize(writeBuffer utils.WriteBuffer) error {
	if pushErr := writeBuffer.PushContext("ChannelInformation"); pushErr != nil {
		return pushErr
	}

	// Simple Field (numChannels)
	numChannels := uint8(m.NumChannels)
	_numChannelsErr := writeBuffer.WriteUint8("numChannels", 3, (numChannels))
	if _numChannelsErr != nil {
		return errors.Wrap(_numChannelsErr, "Error serializing 'numChannels' field")
	}

	// Simple Field (channelCode)
	channelCode := uint16(m.ChannelCode)
	_channelCodeErr := writeBuffer.WriteUint16("channelCode", 13, (channelCode))
	if _channelCodeErr != nil {
		return errors.Wrap(_channelCodeErr, "Error serializing 'channelCode' field")
	}

	if popErr := writeBuffer.PopContext("ChannelInformation"); popErr != nil {
		return popErr
	}
	return nil
}

func (m *ChannelInformation) String() string {
	if m == nil {
		return "<nil>"
	}
	buffer := utils.NewBoxedWriteBufferWithOptions(true, true)
	m.Serialize(buffer)
	return buffer.GetBox().String()
}
