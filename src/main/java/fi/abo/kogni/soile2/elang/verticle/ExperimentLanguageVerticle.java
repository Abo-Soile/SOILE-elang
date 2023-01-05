package fi.abo.kogni.soile2.elang.verticle;

import java.io.StringReader;
import java.nio.charset.Charset;

import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import fi.abo.kogni.soile2.elang.CodeGenerator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class ExperimentLanguageVerticle extends AbstractVerticle {

	private STGroup template1;	
	private String address;
	
	public ExperimentLanguageVerticle(String address) {
		// TODO Auto-generated constructor stub
		this.address = address;
		String fullFileName = ExperimentLanguageVerticle.class.getClassLoader().getResource("elang.stg").getFile();
		template1 = new STGroupFile(fullFileName);
	}
	
    @Override
    public void start() {
        
		System.out.println("Deploying ExperimentLanguageVerticle with id : " + deploymentID());    	
    	System.out.println("Registering Elang Verticle at address " + address);       
        vertx.eventBus().consumer(address, this::getCodeTemplate);                
    }

	@Override
	public void stop(Promise<Void> stopPromise)
	{
		vertx.eventBus().consumer(address, this::getCodeTemplate).unregister().
		onSuccess(v -> stopPromise.complete())
		.onFailure(err -> stopPromise.fail(err));			
	}
	
    private void getCodeTemplate(Message<JsonObject> message) {
    	JsonObject json = message.body();
    	JsonObject reply = new JsonObject();
    	CodeGenerator codeGen = new CodeGenerator(template1);
    	StringBuilder code = new StringBuilder(); 
    	StringBuilder errors = new StringBuilder();
    	codeGen.codeOutputTo(code);
    	codeGen.errorMessagesTo(errors);
    	
    	String temp = json.getString("code");
    	byte[] decoded = temp.getBytes();
    	String encoded = new String(decoded, Charset.defaultCharset());     	
    	StringReader input = new StringReader(encoded);
    	codeGen.generate(input);
    	if (errors.length() > 0) {
    		reply.put("errors", errors.toString());
    	}
    	else {
    		reply.put("code", code.toString());
    	}
    	message.reply(reply);
    }
}

