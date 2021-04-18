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
	"encoding/hex"
	"encoding/xml"
	"fmt"
	"github.com/apache/plc4x/plc4go/internal/plc4go/spi/utils"
	"github.com/pkg/errors"
	"io"
	"strings"
)

// Code generated by build-utils. DO NOT EDIT.

// Constant values.
const BACnetServiceAckReadProperty_OBJECTIDENTIFIERHEADER uint8 = 0x0C
const BACnetServiceAckReadProperty_PROPERTYIDENTIFIERHEADER uint8 = 0x03
const BACnetServiceAckReadProperty_OPENINGTAG uint8 = 0x3E
const BACnetServiceAckReadProperty_CLOSINGTAG uint8 = 0x3F

// The data-structure of this message
type BACnetServiceAckReadProperty struct {
	ObjectType               uint16
	ObjectInstanceNumber     uint32
	PropertyIdentifierLength uint8
	PropertyIdentifier       []int8
	Value                    *BACnetTag
	Parent                   *BACnetServiceAck
}

// The corresponding interface
type IBACnetServiceAckReadProperty interface {
	LengthInBytes() uint16
	LengthInBits() uint16
	Serialize(io utils.WriteBuffer) error
	xml.Marshaler
	xml.Unmarshaler
}

///////////////////////////////////////////////////////////
// Accessors for discriminator values.
///////////////////////////////////////////////////////////
func (m *BACnetServiceAckReadProperty) ServiceChoice() uint8 {
	return 0x0C
}

func (m *BACnetServiceAckReadProperty) InitializeParent(parent *BACnetServiceAck) {
}

func NewBACnetServiceAckReadProperty(objectType uint16, objectInstanceNumber uint32, propertyIdentifierLength uint8, propertyIdentifier []int8, value *BACnetTag) *BACnetServiceAck {
	child := &BACnetServiceAckReadProperty{
		ObjectType:               objectType,
		ObjectInstanceNumber:     objectInstanceNumber,
		PropertyIdentifierLength: propertyIdentifierLength,
		PropertyIdentifier:       propertyIdentifier,
		Value:                    value,
		Parent:                   NewBACnetServiceAck(),
	}
	child.Parent.Child = child
	return child.Parent
}

func CastBACnetServiceAckReadProperty(structType interface{}) *BACnetServiceAckReadProperty {
	castFunc := func(typ interface{}) *BACnetServiceAckReadProperty {
		if casted, ok := typ.(BACnetServiceAckReadProperty); ok {
			return &casted
		}
		if casted, ok := typ.(*BACnetServiceAckReadProperty); ok {
			return casted
		}
		if casted, ok := typ.(BACnetServiceAck); ok {
			return CastBACnetServiceAckReadProperty(casted.Child)
		}
		if casted, ok := typ.(*BACnetServiceAck); ok {
			return CastBACnetServiceAckReadProperty(casted.Child)
		}
		return nil
	}
	return castFunc(structType)
}

func (m *BACnetServiceAckReadProperty) GetTypeName() string {
	return "BACnetServiceAckReadProperty"
}

func (m *BACnetServiceAckReadProperty) LengthInBits() uint16 {
	return m.LengthInBitsConditional(false)
}

func (m *BACnetServiceAckReadProperty) LengthInBitsConditional(lastItem bool) uint16 {
	lengthInBits := uint16(m.Parent.ParentLengthInBits())

	// Const Field (objectIdentifierHeader)
	lengthInBits += 8

	// Simple field (objectType)
	lengthInBits += 10

	// Simple field (objectInstanceNumber)
	lengthInBits += 22

	// Const Field (propertyIdentifierHeader)
	lengthInBits += 5

	// Simple field (propertyIdentifierLength)
	lengthInBits += 3

	// Array field
	if len(m.PropertyIdentifier) > 0 {
		lengthInBits += 8 * uint16(len(m.PropertyIdentifier))
	}

	// Const Field (openingTag)
	lengthInBits += 8

	// Simple field (value)
	lengthInBits += m.Value.LengthInBits()

	// Const Field (closingTag)
	lengthInBits += 8

	return lengthInBits
}

func (m *BACnetServiceAckReadProperty) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func BACnetServiceAckReadPropertyParse(io utils.ReadBuffer) (*BACnetServiceAck, error) {

	// Const Field (objectIdentifierHeader)
	objectIdentifierHeader, _objectIdentifierHeaderErr := io.ReadUint8(8)
	if _objectIdentifierHeaderErr != nil {
		return nil, errors.Wrap(_objectIdentifierHeaderErr, "Error parsing 'objectIdentifierHeader' field")
	}
	if objectIdentifierHeader != BACnetServiceAckReadProperty_OBJECTIDENTIFIERHEADER {
		return nil, errors.New("Expected constant value " + fmt.Sprintf("%d", BACnetServiceAckReadProperty_OBJECTIDENTIFIERHEADER) + " but got " + fmt.Sprintf("%d", objectIdentifierHeader))
	}

	// Simple Field (objectType)
	objectType, _objectTypeErr := io.ReadUint16(10)
	if _objectTypeErr != nil {
		return nil, errors.Wrap(_objectTypeErr, "Error parsing 'objectType' field")
	}

	// Simple Field (objectInstanceNumber)
	objectInstanceNumber, _objectInstanceNumberErr := io.ReadUint32(22)
	if _objectInstanceNumberErr != nil {
		return nil, errors.Wrap(_objectInstanceNumberErr, "Error parsing 'objectInstanceNumber' field")
	}

	// Const Field (propertyIdentifierHeader)
	propertyIdentifierHeader, _propertyIdentifierHeaderErr := io.ReadUint8(5)
	if _propertyIdentifierHeaderErr != nil {
		return nil, errors.Wrap(_propertyIdentifierHeaderErr, "Error parsing 'propertyIdentifierHeader' field")
	}
	if propertyIdentifierHeader != BACnetServiceAckReadProperty_PROPERTYIDENTIFIERHEADER {
		return nil, errors.New("Expected constant value " + fmt.Sprintf("%d", BACnetServiceAckReadProperty_PROPERTYIDENTIFIERHEADER) + " but got " + fmt.Sprintf("%d", propertyIdentifierHeader))
	}

	// Simple Field (propertyIdentifierLength)
	propertyIdentifierLength, _propertyIdentifierLengthErr := io.ReadUint8(3)
	if _propertyIdentifierLengthErr != nil {
		return nil, errors.Wrap(_propertyIdentifierLengthErr, "Error parsing 'propertyIdentifierLength' field")
	}

	// Array field (propertyIdentifier)
	// Count array
	propertyIdentifier := make([]int8, propertyIdentifierLength)
	for curItem := uint16(0); curItem < uint16(propertyIdentifierLength); curItem++ {
		_item, _err := io.ReadInt8(8)
		if _err != nil {
			return nil, errors.Wrap(_err, "Error parsing 'propertyIdentifier' field")
		}
		propertyIdentifier[curItem] = _item
	}

	// Const Field (openingTag)
	openingTag, _openingTagErr := io.ReadUint8(8)
	if _openingTagErr != nil {
		return nil, errors.Wrap(_openingTagErr, "Error parsing 'openingTag' field")
	}
	if openingTag != BACnetServiceAckReadProperty_OPENINGTAG {
		return nil, errors.New("Expected constant value " + fmt.Sprintf("%d", BACnetServiceAckReadProperty_OPENINGTAG) + " but got " + fmt.Sprintf("%d", openingTag))
	}

	// Simple Field (value)
	value, _valueErr := BACnetTagParse(io)
	if _valueErr != nil {
		return nil, errors.Wrap(_valueErr, "Error parsing 'value' field")
	}

	// Const Field (closingTag)
	closingTag, _closingTagErr := io.ReadUint8(8)
	if _closingTagErr != nil {
		return nil, errors.Wrap(_closingTagErr, "Error parsing 'closingTag' field")
	}
	if closingTag != BACnetServiceAckReadProperty_CLOSINGTAG {
		return nil, errors.New("Expected constant value " + fmt.Sprintf("%d", BACnetServiceAckReadProperty_CLOSINGTAG) + " but got " + fmt.Sprintf("%d", closingTag))
	}

	// Create a partially initialized instance
	_child := &BACnetServiceAckReadProperty{
		ObjectType:               objectType,
		ObjectInstanceNumber:     objectInstanceNumber,
		PropertyIdentifierLength: propertyIdentifierLength,
		PropertyIdentifier:       propertyIdentifier,
		Value:                    value,
		Parent:                   &BACnetServiceAck{},
	}
	_child.Parent.Child = _child
	return _child.Parent, nil
}

func (m *BACnetServiceAckReadProperty) Serialize(io utils.WriteBuffer) error {
	ser := func() error {

		// Const Field (objectIdentifierHeader)
		_objectIdentifierHeaderErr := io.WriteUint8("objectIdentifierHeader", 8, 0x0C)
		if _objectIdentifierHeaderErr != nil {
			return errors.Wrap(_objectIdentifierHeaderErr, "Error serializing 'objectIdentifierHeader' field")
		}

		// Simple Field (objectType)
		objectType := uint16(m.ObjectType)
		_objectTypeErr := io.WriteUint16("objectType", 10, (objectType))
		if _objectTypeErr != nil {
			return errors.Wrap(_objectTypeErr, "Error serializing 'objectType' field")
		}

		// Simple Field (objectInstanceNumber)
		objectInstanceNumber := uint32(m.ObjectInstanceNumber)
		_objectInstanceNumberErr := io.WriteUint32("objectInstanceNumber", 22, (objectInstanceNumber))
		if _objectInstanceNumberErr != nil {
			return errors.Wrap(_objectInstanceNumberErr, "Error serializing 'objectInstanceNumber' field")
		}

		// Const Field (propertyIdentifierHeader)
		_propertyIdentifierHeaderErr := io.WriteUint8("propertyIdentifierHeader", 5, 0x03)
		if _propertyIdentifierHeaderErr != nil {
			return errors.Wrap(_propertyIdentifierHeaderErr, "Error serializing 'propertyIdentifierHeader' field")
		}

		// Simple Field (propertyIdentifierLength)
		propertyIdentifierLength := uint8(m.PropertyIdentifierLength)
		_propertyIdentifierLengthErr := io.WriteUint8("propertyIdentifierLength", 3, (propertyIdentifierLength))
		if _propertyIdentifierLengthErr != nil {
			return errors.Wrap(_propertyIdentifierLengthErr, "Error serializing 'propertyIdentifierLength' field")
		}

		// Array Field (propertyIdentifier)
		if m.PropertyIdentifier != nil {
			for _, _element := range m.PropertyIdentifier {
				_elementErr := io.WriteInt8("", 8, _element)
				if _elementErr != nil {
					return errors.Wrap(_elementErr, "Error serializing 'propertyIdentifier' field")
				}
			}
		}

		// Const Field (openingTag)
		_openingTagErr := io.WriteUint8("openingTag", 8, 0x3E)
		if _openingTagErr != nil {
			return errors.Wrap(_openingTagErr, "Error serializing 'openingTag' field")
		}

		// Simple Field (value)
		_valueErr := m.Value.Serialize(io)
		if _valueErr != nil {
			return errors.Wrap(_valueErr, "Error serializing 'value' field")
		}

		// Const Field (closingTag)
		_closingTagErr := io.WriteUint8("closingTag", 8, 0x3F)
		if _closingTagErr != nil {
			return errors.Wrap(_closingTagErr, "Error serializing 'closingTag' field")
		}

		return nil
	}
	return m.Parent.SerializeParent(io, m, ser)
}

func (m *BACnetServiceAckReadProperty) UnmarshalXML(d *xml.Decoder, start xml.StartElement) error {
	var token xml.Token
	var err error
	foundContent := false
	token = start
	for {
		switch token.(type) {
		case xml.StartElement:
			foundContent = true
			tok := token.(xml.StartElement)
			switch tok.Name.Local {
			case "objectType":
				var data uint16
				if err := d.DecodeElement(&data, &tok); err != nil {
					return err
				}
				m.ObjectType = data
			case "objectInstanceNumber":
				var data uint32
				if err := d.DecodeElement(&data, &tok); err != nil {
					return err
				}
				m.ObjectInstanceNumber = data
			case "propertyIdentifierLength":
				var data uint8
				if err := d.DecodeElement(&data, &tok); err != nil {
					return err
				}
				m.PropertyIdentifierLength = data
			case "propertyIdentifier":
				var _encoded string
				if err := d.DecodeElement(&_encoded, &tok); err != nil {
					return err
				}
				_decoded, err := hex.DecodeString(_encoded)
				_len := len(_decoded)
				if err != nil {
					return err
				}
				m.PropertyIdentifier = utils.ByteArrayToInt8Array(_decoded[0:_len])
			case "value":
				var dt *BACnetTag
				if err := d.DecodeElement(&dt, &tok); err != nil {
					if err == io.EOF {
						continue
					}
					return err
				}
				m.Value = dt
			}
		}
		token, err = d.Token()
		if err != nil {
			if err == io.EOF && foundContent {
				return nil
			}
			return err
		}
	}
}

func (m *BACnetServiceAckReadProperty) MarshalXML(e *xml.Encoder, start xml.StartElement) error {
	if err := e.EncodeElement(m.ObjectType, xml.StartElement{Name: xml.Name{Local: "objectType"}}); err != nil {
		return err
	}
	if err := e.EncodeElement(m.ObjectInstanceNumber, xml.StartElement{Name: xml.Name{Local: "objectInstanceNumber"}}); err != nil {
		return err
	}
	if err := e.EncodeElement(m.PropertyIdentifierLength, xml.StartElement{Name: xml.Name{Local: "propertyIdentifierLength"}}); err != nil {
		return err
	}
	_encodedPropertyIdentifier := hex.EncodeToString(utils.Int8ArrayToByteArray(m.PropertyIdentifier))
	_encodedPropertyIdentifier = strings.ToUpper(_encodedPropertyIdentifier)
	if err := e.EncodeElement(_encodedPropertyIdentifier, xml.StartElement{Name: xml.Name{Local: "propertyIdentifier"}}); err != nil {
		return err
	}
	if err := e.EncodeElement(m.Value, xml.StartElement{Name: xml.Name{Local: "value"}}); err != nil {
		return err
	}
	return nil
}

func (m BACnetServiceAckReadProperty) String() string {
	return string(m.Box("", 120))
}

func (m BACnetServiceAckReadProperty) Box(name string, width int) utils.AsciiBox {
	boxName := "BACnetServiceAckReadProperty"
	if name != "" {
		boxName += "/" + name
	}
	childBoxer := func() []utils.AsciiBox {
		boxes := make([]utils.AsciiBox, 0)
		// Const Field (objectIdentifierHeader)
		boxes = append(boxes, utils.BoxAnything("ObjectIdentifierHeader", uint8(0x0C), -1))
		// Simple field (case simple)
		// uint16 can be boxed as anything with the least amount of space
		boxes = append(boxes, utils.BoxAnything("ObjectType", m.ObjectType, -1))
		// Simple field (case simple)
		// uint32 can be boxed as anything with the least amount of space
		boxes = append(boxes, utils.BoxAnything("ObjectInstanceNumber", m.ObjectInstanceNumber, -1))
		// Const Field (propertyIdentifierHeader)
		boxes = append(boxes, utils.BoxAnything("PropertyIdentifierHeader", uint8(0x03), -1))
		// Simple field (case simple)
		// uint8 can be boxed as anything with the least amount of space
		boxes = append(boxes, utils.BoxAnything("PropertyIdentifierLength", m.PropertyIdentifierLength, -1))
		// Array Field (propertyIdentifier)
		if m.PropertyIdentifier != nil {
			// Simple array base type int8 will be rendered one by one
			arrayBoxes := make([]utils.AsciiBox, 0)
			for _, _element := range m.PropertyIdentifier {
				arrayBoxes = append(arrayBoxes, utils.BoxAnything("", _element, width-2))
			}
			boxes = append(boxes, utils.BoxBox("PropertyIdentifier", utils.AlignBoxes(arrayBoxes, width-4), 0))
		}
		// Const Field (openingTag)
		boxes = append(boxes, utils.BoxAnything("OpeningTag", uint8(0x3E), -1))
		// Complex field (case complex)
		boxes = append(boxes, m.Value.Box("value", width-2))
		// Const Field (closingTag)
		boxes = append(boxes, utils.BoxAnything("ClosingTag", uint8(0x3F), -1))
		return boxes
	}
	return m.Parent.BoxParent(boxName, width, childBoxer)
}
