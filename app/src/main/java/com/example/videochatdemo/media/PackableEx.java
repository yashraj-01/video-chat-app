package com.example.videochatdemo.media;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
