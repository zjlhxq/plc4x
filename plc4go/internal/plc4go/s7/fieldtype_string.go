// Code generated by "stringer -type FieldType"; DO NOT EDIT.

package s7

import "strconv"

func _() {
	// An "invalid array index" compiler error signifies that the constant values have changed.
	// Re-run the stringer command to generate them again.
	var x [1]struct{}
	_ = x[FIELD-0]
	_ = x[STRING_FIELD-1]
}

const _FieldType_name = "FIELDSTRING_FIELD"

var _FieldType_index = [...]uint8{0, 5, 17}

func (i FieldType) String() string {
	if i >= FieldType(len(_FieldType_index)-1) {
		return "FieldType(" + strconv.FormatInt(int64(i), 10) + ")"
	}
	return _FieldType_name[_FieldType_index[i]:_FieldType_index[i+1]]
}
