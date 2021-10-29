package me.bytebeats.routerx.agp.util

import org.gradle.api.Project
import org.gradle.api.logging.Logger

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2021/10/29 14:22
 * @Version 1.0
 * @Description 日志记录
 */

class LoggerX {
    static Logger logger

    static void make(Project project) {
        logger = project.logger
    }

    static void i(String info) {
        if (logger != null && info != null) {
            logger.info("RouterX::Register >>>  ${info}")
        }
    }

    static void w(String warning) {
        if (logger != null && warning != null) {
            logger.warn("RouterX::Register >>>  ${warning}")
        }
    }

    static void e(String error) {
        if (logger != null && error != null) {
            logger.error("RouterX::Register >>>  ${error}")
        }
    }
}
