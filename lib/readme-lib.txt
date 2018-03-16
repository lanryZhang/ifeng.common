在运行环境下，包的类库依赖关系如下：

com
  |-ifeng
	    |-common
	        |-conf
	        |    {com.ifeng.common.misc}
	        |    {commons-beanutils} 
	        |    {commons-collections}
	        |    {commons-logging}
	        |    
	        |-misc
	        |    {commons-beanutils}
	        |    {commons-lang3}
	        |    {commons-collections}
	        |    {commons-logging}
	        |    {log4j}
	        |
	        |-plugin
	        |    {com.ifeng.common.conf}
	        |
	        |-dm
	        |    {com.ifeng.common.misc}
	        |    {com.ifeng.common.conf}
	        |    {hibernate}
	        |        {dom4j}
	        |        {ehcache}
	        |        {jta}
	        |        {cglib}
	        |        {asm}
	        |        {antlr}
	        |    {commons-lang3}
	        |    {commons-collections}
	        |    {commons-logging}
		|    {concurrentlinkedhashmap-lru}
	        |
	        |-remoting
	        |    {com.ifeng.common.misc}
	        |    {com.ifeng.common.conf}
	        |    {cglib}
	        


当需要运行common的单元测试代码时,需要lib/test下的类库。
当需要运行emma检查覆盖率时，除了测试类库外，还需要lib/emma下的类库。
当需要ant能力时，需要lib/ant下的类库，其中svn子目录为ant下的svn集成能力。