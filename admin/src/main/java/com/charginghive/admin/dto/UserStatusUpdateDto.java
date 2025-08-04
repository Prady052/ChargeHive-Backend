package com.charginghive.admin.dto;

import lombok.Data;

@Data
public class UserStatusUpdateDto {
    private Long userId;
    private boolean enabled; // true to enable/unblock, false to disable/block.
}
