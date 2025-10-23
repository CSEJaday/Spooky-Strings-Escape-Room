package com.model;

import java.util.*;
/**
 * Very small JSON parser that returns:
 *  - java.util.Map<String,Object> for JSON objects
 *  - java.util.List<Object> for JSON arrays
 *  - java.lang.String for JSON strings
 *  - java.lang.Number (Double or Long) for numbers
 *  - java.lang.Boolean for booleans
 *  - null for JSON null
 *
 * This parser is intentionally compact and handles normal JSON used for escape-room files.
 * It supports string escapes: \", \\, \/, \b, \f, \n, \r, \t, and unicode \\uXXXX.
 */
public final class JsonSimpleParser {
    private final String s;
    private int pos = 0;
    private JsonSimpleParser(String s) { this.s = s; }

    public static Object parse(String text) {
        JsonSimpleParser p = new JsonSimpleParser(text);
        p.skipWhitespace();
        Object v = p.parseValue();
        p.skipWhitespace();
        if (p.pos != p.s.length()) throw new RuntimeException("Extra data after JSON end at pos " + p.pos);
        return v;
    }

    private Object parseValue() {
        skipWhitespace();
        if (pos >= s.length()) throw new RuntimeException("Unexpected end of input");
        char c = s.charAt(pos);
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') { parseNull(); return null; }
        if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();
        throw new RuntimeException("Unexpected char at pos " + pos + ": " + c);
    }

    private Map<String,Object> parseObject() {
        Map<String,Object> obj = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();
        if (peek() == '}') { expect('}'); return obj; }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object value = parseValue();
            obj.put(key, value);
            skipWhitespace();
            char c = peek();
            if (c == ',') { expect(','); continue; }
            if (c == '}') { expect('}'); break; }
            throw new RuntimeException("Expected , or } in object at pos " + pos);
        }
        return obj;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        expect('[');
        skipWhitespace();
        if (peek() == ']') { expect(']'); return list; }
        while (true) {
            skipWhitespace();
            Object v = parseValue();
            list.add(v);
            skipWhitespace();
            char c = peek();
            if (c == ',') { expect(','); continue; }
            if (c == ']') { expect(']'); break; }
            throw new RuntimeException("Expected , or ] in array at pos " + pos);
        }
        return list;
    }

    private String parseString() {
    expect('"');
    StringBuilder sb = new StringBuilder();
    while (pos < s.length()) {
        char c = s.charAt(pos++);
        if (c == '"') break; // end of string
        if (c == '\\') {
            if (pos >= s.length()) throw new RuntimeException("Unterminated escape in string");
            char e = s.charAt(pos++);
            switch (e) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    // try to read 4 hex digits
                    if (pos + 4 <= s.length()) {
                        String hex = s.substring(pos, pos + 4);
                        boolean valid = true;
                        for (int i = 0; i < 4; i++) {
                            char h = hex.charAt(i);
                            boolean isHex = (h >= '0' && h <= '9') ||
                                            (h >= 'A' && h <= 'F') ||
                                            (h >= 'a' && h <= 'f');
                            if (!isHex) { valid = false; break; }
                        }
                        if (valid) {
                            try {
                                int code = Integer.parseInt(hex, 16);
                                sb.append((char) code);
                                pos += 4;
                                break;
                            } catch (NumberFormatException ex) {
                                // invalid hex, fall through to replacement
                            }
                        }
                    }
                    // invalid or incomplete \\u - append replacement char instead of throwing
                    sb.append('\uFFFD'); // replacement character \uFFFD
                    break;
                default:
                    // unknown escape, keep raw char to be forgiving
                    sb.append(e);
                    break;
            }
        } else {
            sb.append(c);
        }
    }
    return sb.toString();
}



    private Number parseNumber() {
        int start = pos;
        if (peek() == '-') pos++;
        while (pos < s.length() && java.lang.Character.isDigit(s.charAt(pos))) pos++;
        boolean isFractional = false;
        if (pos < s.length() && s.charAt(pos) == '.') {
            isFractional = true;
            pos++;
            while (pos < s.length() && java.lang.Character.isDigit(s.charAt(pos))) pos++;
        }
        if (pos < s.length()) {
            char c = s.charAt(pos);
            if (c == 'e' || c == 'E') {
                isFractional = true;
                pos++;
                if (pos < s.length() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) pos++;
                while (pos < s.length() && java.lang.Character.isDigit(s.charAt(pos))) pos++;
            }
        }
        String num = s.substring(start, pos);
        try {
            if (isFractional) {
                return Double.parseDouble(num);
            } else {
                long lv = Long.parseLong(num);
                if (lv >= Integer.MIN_VALUE && lv <= Integer.MAX_VALUE) return (int) lv;
                return lv;
            }
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid number '" + num + "' at pos " + start);
        }
    }

    private Boolean parseBoolean() {
        if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
        if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        throw new RuntimeException("Invalid boolean token at pos " + pos);
    }

    private void parseNull() {
        if (s.startsWith("null", pos)) { pos += 4; return; }
        throw new RuntimeException("Invalid token at pos " + pos);
    }

    private void skipWhitespace() {
        while (pos < s.length()) {
            char c = s.charAt(pos);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') pos++; else break;
        }
    }

    private char peek() {
        if (pos >= s.length()) return '\0';
        return s.charAt(pos);
    }

    private void expect(char expected) {
        if (pos >= s.length() || s.charAt(pos) != expected) {
            throw new RuntimeException("Expected '" + expected + "' at pos " + pos + " but found '" +
                    (pos < s.length() ? s.charAt(pos) : "EOF") + "'");
        }
        pos++;
    }
}
