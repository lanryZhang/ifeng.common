package com.ifeng.common.dm;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.misc.Logger;
/**
 * <title>QueuedWriteDataManager </title>
 * 
 * <pre>提供写异步能力的DataManager。采用固定线程数的Executor来完成写操作。
 * 
 * </pre>
 * Copyright © 2011 Phoenix New Media Limited All Rights Reserved. 
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class QueuedWriteDataManager extends AbstractDataManagerDecorator {
	private static final Logger log = Logger
			.getLogger(QueuedWriteDataManager.class);

	private Executor executor;

	public QueuedWriteDataManager() {
		// for config
	}

	/**
	 * <pre>
	 *    &lt;... type=&quot;com.ifeng.common.dm.QueuedWriteDataManager&quot;&gt;
	 *      &lt;data-manager ... 内部datamanager的配置/&gt;
	 *      &lt;thread-num ... executor的线程数/&gt;
	 *    &lt;/...&gt;
	 * </pre>
	 */
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) {
		this.executor = Executors.newFixedThreadPool((Integer) configRoot
				.createChildObject(this, configEle, "thread-num", true,
						Integer.class));
		return super.config(configRoot, parent, configEle);
	}

	/*
	 * add总是返回null，调用者不能依赖于返回值
	 * 
	 * @see com.ifeng.common.dm.AbstractDataManagerDecorator#add(java.lang.Object,
	 *      java.util.Map)
	 */
	public Object add(final Object obj, final Map params)
			throws DataManagerException {
		try {
			this.executor.execute(new Runnable() {
				public void run() {
					try {
						getManager().add(obj, params);
					} catch (DataManagerException e) {
						log.error("Error in queued add", e);
					}
				}
			});
		} catch (Exception e) {
			throw new DataManagerException(e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.AbstractDataManagerDecorator#delete(java.lang.Object,
	 *      java.util.Map)
	 */
	public void delete(final Object obj, final Map params)
			throws DataManagerException {
		try {
			this.executor.execute(new Runnable() {
				public void run() {
					try {
						getManager().delete(obj, params);
					} catch (DataManagerException e) {
						log.error("Error in queued delete", e);
					}
				}
			});
		} catch (Exception e) {
			throw new DataManagerException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ifeng.common.dm.AbstractDataManagerDecorator#modify(java.lang.Object,
	 *      java.lang.String[], java.util.Map)
	 */
	public void modify(final Object obj, final String[] fields, final Map params)
			throws DataManagerException {
		try {
			this.executor.execute(new Runnable() {
				public void run() {
					try {
						getManager().modify(obj, fields, params);
					} catch (DataManagerException e) {
						log.error("Error in queued modify", e);
					}
				}
			});
		} catch (Exception e) {
			throw new DataManagerException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.dm.AbstractDataManagerDecorator#deepAdd(java.lang.Object, java.util.Map)
	 */
	public Object deepAdd(final Object obj, final Map params) throws DataManagerException {
		try {
            this.executor.execute(new Runnable() {
                public void run() {
                    try {
                        getManager().deepAdd(obj, params);
                    } catch (DataManagerException e) {
                        log.error("Error in queued add", e);
                    }
                }
            });
        } catch (Exception e) {
            throw new DataManagerException(e);
        }
        return null;
	}

	/* (non-Javadoc)
	 * @see com.ifeng.common.dm.AbstractDataManagerDecorator#deepModify(java.lang.Object, java.lang.String[], java.util.Map)
	 */
	public void deepModify(final Object obj, final String[] fields, final Map params) throws DataManagerException {
        try {
            this.executor.execute(new Runnable() {
                public void run() {
                    try {
                        getManager().deepModify(obj, fields, params);
                    } catch (DataManagerException e) {
                        log.error("Error in queued modify", e);
                    }
                }
            });
        } catch (Exception e) {
            throw new DataManagerException(e);
        }
	}
}
