import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import  com.neuronrobotics.bowlerstudio.physics.*;
import com.neuronrobotics.bowlerstudio.threed.*;import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.vecmath.Vector3f;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;

import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.ILinkListener;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;
import com.neuronrobotics.sdk.util.ThreadUtil;

import Jama.Matrix;
import eu.mihosoft.vrl.v3d.CSG;
import javafx.application.Platform;
import javafx.scene.transform.Affine;
MobileBase base;
java.util.List<String> possible = DeviceManager.listConnectedDevice(MobileBase.class)
Object dev ;
if(possible.size()==1){
	dev=DeviceManager.getSpecificDevice(MobileBase.class, possible.get(0));
}
//Object dev = DeviceManager.getSpecificDevice(MobileBase.class, "cartWalker");
//Object dev = DeviceManager.getSpecificDevice(MobileBase.class, "CarlTheWalkingRobot");
println "found: "+dev
//Check if the device already exists in the device Manager
if(dev==null){
	//Create the kinematics model from the xml file describing the D-H compliant parameters. 
	def file=["https://gist.github.com/bcb4760a449190206170.git","CarlTheRobot.xml"]as String[]
	//def file=["https://gist.github.com/bcb4760a449190206170.git","CarlTheRobot.xml"]as String[]
	String xmlContent = ScriptingEngine.codeFromGit(file[0],file[1])[0];
	MobileBase mb =new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"));
	mb.setGitSelfSource(file);
	mb.connect();
	DeviceManager.addConnection(mb,mb.getScriptingName())
	base = mb;
	ThreadUtil.wait(1000)
}else{
	println "Arm found, runing code"
	//the device is already present on the system, load the one that exists.
  	base=(MobileBase)dev
}

base.DriveArc(new TransformNR(), 0);
PhysicsCore core = PhysicsEngine.get()
PhysicsEngine.clear();

MobileBasePhysicsManager m;

while(MobileBaseCadManager.get( base).getProcesIndictor().getProgress()<1){
	println "Waiting for cad to get to 1:"+MobileBaseCadManager.get( base).getProcesIndictor().getProgress()
	ThreadUtil.wait(1000)
}

HashMap<DHLink, CSG> simplecad = MobileBaseCadManager.getSimplecad(base) 
def baseCad=MobileBaseCadManager.getBaseCad(base)
m = new MobileBasePhysicsManager(base, baseCad, simplecad);

Thread t =new Thread({
	// walk forward 10 increments of 10 mm totalling 100 mm translation
	TransformNR move = new TransformNR(10,0,0,new RotationNR( 0,0, 2))
	double toSeconds=0.1//100 ms for each increment
	for(int i=0;i<50;i++){
		base.DriveArc(move, toSeconds);
		ThreadUtil.wait((int)toSeconds*1000)
	}
})
t.start()
int msLoopTime =200;
BowlerStudioController.setCsg(PhysicsEngine.getCsgFromEngine());
// run the physics engine for a few cycles
for (int i = 0; i < 600&& !Thread.interrupted(); i++) {
	long start = System.currentTimeMillis();
	PhysicsEngine.stepMs(msLoopTime);
	long took = (System.currentTimeMillis() - start);
	if (took < msLoopTime){
		ThreadUtil.wait((int) (msLoopTime - took)/4);
		
	}else{
		//System.gc()
		println "Real time broken! took "+took
		System.gc()
		if(took>2000)
		 return null;
	}
	//if(i%100==0)
	//	new Thread({System.gc()}).start()
 }
