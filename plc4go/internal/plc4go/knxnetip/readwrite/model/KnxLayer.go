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
	"fmt"
	"github.com/apache/plc4x/plc4go/internal/plc4go/spi/utils"
	"io"
)

// Code generated by build-utils. DO NOT EDIT.

type KnxLayer uint8

type IKnxLayer interface {
	Serialize(io utils.WriteBuffer) error
	xml.Marshaler
	xml.Unmarshaler
}

const (
	KnxLayer_TUNNEL_LINK_LAYER KnxLayer = 0x02
	KnxLayer_TUNNEL_RAW        KnxLayer = 0x04
	KnxLayer_TUNNEL_BUSMONITOR KnxLayer = 0x80
)

var KnxLayerValues []KnxLayer

func init() {
	KnxLayerValues = []KnxLayer{
		KnxLayer_TUNNEL_LINK_LAYER,
		KnxLayer_TUNNEL_RAW,
		KnxLayer_TUNNEL_BUSMONITOR,
	}
}

func KnxLayerByValue(value uint8) KnxLayer {
	switch value {
	case 0x02:
		return KnxLayer_TUNNEL_LINK_LAYER
	case 0x04:
		return KnxLayer_TUNNEL_RAW
	case 0x80:
		return KnxLayer_TUNNEL_BUSMONITOR
	}
	return 0
}

func KnxLayerByName(value string) KnxLayer {
	switch value {
	case "TUNNEL_LINK_LAYER":
		return KnxLayer_TUNNEL_LINK_LAYER
	case "TUNNEL_RAW":
		return KnxLayer_TUNNEL_RAW
	case "TUNNEL_BUSMONITOR":
		return KnxLayer_TUNNEL_BUSMONITOR
	}
	return 0
}

func CastKnxLayer(structType interface{}) KnxLayer {
	castFunc := func(typ interface{}) KnxLayer {
		if sKnxLayer, ok := typ.(KnxLayer); ok {
			return sKnxLayer
		}
		return 0
	}
	return castFunc(structType)
}

func (m KnxLayer) LengthInBits() uint16 {
	return 8
}

func (m KnxLayer) LengthInBytes() uint16 {
	return m.LengthInBits() / 8
}

func KnxLayerParse(io utils.ReadBuffer) (KnxLayer, error) {
	val, err := io.ReadUint8(8)
	if err != nil {
		return 0, nil
	}
	return KnxLayerByValue(val), nil
}

func (e KnxLayer) Serialize(io utils.WriteBuffer) error {
	err := io.WriteUint8("KnxLayer", 8, uint8(e))
	return err
}

func (m *KnxLayer) UnmarshalXML(d *xml.Decoder, start xml.StartElement) error {
	var token xml.Token
	var err error
	for {
		token, err = d.Token()
		if err != nil {
			if err == io.EOF {
				return nil
			}
			return err
		}
		switch token.(type) {
		case xml.CharData:
			tok := token.(xml.CharData)
			*m = KnxLayerByName(string(tok))
		}
	}
}

func (m KnxLayer) MarshalXML(e *xml.Encoder, start xml.StartElement) error {
	if err := e.EncodeElement(m.String(), start); err != nil {
		return err
	}
	return nil
}

func (e KnxLayer) name() string {
	switch e {
	case KnxLayer_TUNNEL_LINK_LAYER:
		return "TUNNEL_LINK_LAYER"
	case KnxLayer_TUNNEL_RAW:
		return "TUNNEL_RAW"
	case KnxLayer_TUNNEL_BUSMONITOR:
		return "TUNNEL_BUSMONITOR"
	}
	return ""
}

func (e KnxLayer) String() string {
	return e.name()
}

func (m KnxLayer) Box(s string, i int) utils.AsciiBox {
	boxName := "KnxLayer"
	if s != "" {
		boxName += "/" + s
	}
	return utils.BoxString(boxName, fmt.Sprintf("%#0*x %s", 2, uint8(m), m.name()), -1)
}
