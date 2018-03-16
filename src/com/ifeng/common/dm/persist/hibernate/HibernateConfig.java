package com.ifeng.common.dm.persist.hibernate;

import java.net.URL;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.XmlLoader;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
/**
 * <title>Hibernate的配置</title>
 * 
 * <pre>Hibernate的配置类，单例模式.<br>
 * 以下内容仅为示例，实际上config方法由另外一个Configurable调用.
 * &lt; ... config-file="hibernate配置文件名"/&gt;
 * 或
 *  &lt; ... &gt;`
 *    &lt;session-factory&gt;...
 *    Hibernate配置
 *    ...
 *  &lt;/...&gt;
 *  <br>
 * 特别提示：最好不要在Hibernate配置中配置mapping，而要在具体模块中用MappingConfig来配置，
 * 以降低模块之间的依赖程度
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public final class HibernateConfig {

    private static final Logger log = Logger.getLogger(HibernateConfig.class);

    private static SessionFactory sessionFactory;
    private static Configuration config = new Configuration();

    private HibernateConfig() {
        // utility class
    }
    
    /**
     * 嵌入的配置
     */
    public static void config(ConfigRoot configRoot, Element configEle) {
        String file = XmlLoader.getAttribute(configEle, "config-file", null);
        // 默认 protocol 为 null
        String protocol = XmlLoader.getAttribute(configEle, "protocol", null);

        if (file != null) {
            URL url = configRoot.getConfigFileURL(file, protocol);
            addConfig(url);
        } else {
            Element ele = XmlLoader.getChildElement(configEle, null,
                    "session-factory");
            Document doc = new DocumentImpl();
            Element docEle = doc.createElement("hibernate-configuration");
            docEle.appendChild(doc.importNode(ele, true));
            doc.appendChild(docEle);
            addConfig(doc);
        }
    }

    /**
     * Configuration.
     * @param fullPathName Config file full pathname.
     */
    public static void addConfig(String fullPathName) {
        Document doc = null;
        try {
            doc = XmlLoader.parseXMLDocument(fullPathName, System.getProperties());
        } catch (Throwable e) {
            log.error("Error loading hibernate config file: " + fullPathName, e);
            throw new RuntimeException(e);
        }
        addConfig(doc);
    }
    
    /**
     * Configuration.
     * @param url Config file URL
     */
    public static void addConfig(URL url) {
        Document doc = null;
        try {
            doc = XmlLoader.parseXMLDocument(url, System.getProperties());
        } catch (Throwable e) {
            log.error("Error loading hibernate config file: " + url, e);
            throw new RuntimeException(e);
        }
        addConfig(doc);
    }
    
    /**
     * 添加config，按照hibernate.cfg.xml的格式
     */
    public static synchronized void addConfig(Document document) {
        resetSessionFactory();
        config.configure(document);
    }

    private static void resetSessionFactory() {
        if (sessionFactory != null) {
            log.error("addConfig: sessionFactory already created. "
                    + "Please correct the init sequence to add all config "
                    + "before sessionFactory is used");
            // to recreate the session factory
            sessionFactory = null;
        }
    }

    public static synchronized void addMapping(Class clazz) {
        resetSessionFactory();
        config.addClass(clazz);
    }
    
    public static synchronized void addMapping(URL url) {
        resetSessionFactory();
        config.addURL(url);
    }
    
    public static synchronized void addMappingFromFile(String fullPath) {
        resetSessionFactory();
        config.addFile(fullPath);
    }
    
    /**
     * 从resource(类路径中)中读取mapping配置
     * @param resource
     */
    public static synchronized void addMappingFromResource(String resource) {
        resetSessionFactory();
        config.addResource(resource);
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = config.buildSessionFactory();
        }
        return sessionFactory;
    }

    public static Configuration getConfiguration() {
        return config;
    }

}

