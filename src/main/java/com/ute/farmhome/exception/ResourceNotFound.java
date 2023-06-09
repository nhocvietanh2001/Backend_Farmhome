package com.ute.farmhome.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceNotFound extends RuntimeException {
    private String entity;
    private String field;
    private String value;
    public ResourceNotFound(String msg)
    {
        super(msg);
    }
    @Override
    public String toString() {
        return String.format("%s not found", entity);
    }
}
