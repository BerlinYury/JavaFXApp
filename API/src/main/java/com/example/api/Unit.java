package com.example.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;


@Getter
@RequiredArgsConstructor
public abstract class Unit  implements Serializable {
        protected final String id;
        protected final String name;
}
