s.options.memSize_(65536 * 24);
s.boot;

// frequency of 1/4.0 s

// envelope and lowpass some pink noise?

SynthDef(\wump, {
	arg out=0,freq=400,attack=0.01, release=1.2, level=1, curve=(-2);
	var env = Env.perc(attack, release, level, curve);
	Out.ar(out,
		EnvGen.kr(env, doneAction:2)
		*
		LPF.ar(PinkNoise.ar(),freq*(BrownNoise.kr(0.1,1.0))));
}).load(s);

SynthDef(\whack, {
	|out=0,delay=0.2,decay=3.0,lp=1000,attack=0.01,release=2.2,level=1|
	var curve=(-2);
	var env = Env.perc(attack, 1.0+release, level, curve);

	Out.ar(out,
		EnvGen.kr(env, doneAction: 2) * GVerb.ar(LPF.ar(CombC.ar(Decay.ar(Dust.ar(1,0.5), 0.2, WhiteNoise.ar), 10.2, delay, decay),lp)));
}).load(s);

//Synth(\whack)

SynthDef(\drones, {
	|out=0,amp=1.0,freq=440.0,mix=0.6,room=0.25,damp=0.5,lofreq=0.1|
	var nsize,n = (2..10);
	var modulation = BrownNoise.kr(0.01,1.0);
	nsize = n.size;
	
	Out.ar(0,
		FreeVerb.ar(
			LPF.ar(
				amp * 
				(
					n.collect {arg i; 
						SinOsc.ar( modulation*(1.0 - (1.0/(i*i))) * freq )
					}).sum / nsize
				, (12000*SinOsc.kr(lofreq,mul:0.5,add:0.5))+100),
			mix,
			room,
			damp
		)
	)
}).add;

SynthDef(\shore, {| out = 0, amp = 1.0, bufnum = 0 |
    Out.ar(out,
        amp * PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), loop: 1.0)
    )
}).load(s);



~drones1 = Synth(\drones,[\amp,0.1,\freq,550,\lofreq,1/16.0]);
~drones2 = Synth(\drones,[\amp,0.1,\freq,440,\lofreq,1/8.0]);

~release = 0.1;
~rot = Routine({
	loop {
		~release = (~release*((0.1.rand) + 1.0)).min(4.4);
		Synth(\whack,[\release,~release]);
		Synth(\wump,[\release,~release]);
		~release.postln;
		4.0.wait;
	}	
}).play;

~shore = Buffer.read(s, "watershore.wav"); // remember to free the buffer later.

~shorep = Synth(\shore, [\out, 0, \amp, 0.3, \bufnum, ~shore]);

// metallic 
// { GVerb.ar(LPF.ar(CombC.ar(Decay.ar(Dust.ar(1,0.5), 0.2, WhiteNoise.ar), 0.2, 0.2, 3),1000)) }.play;

// { Streson.ar(Dust(200)) }.play

// { Streson.ar(LFSaw.ar([220, 180], 0, mul:EnvGen.kr(Env.asr(0.5, 1, 0.02), 1.0) * 0.2), LinExp.kr(LFCub.kr(0.1, 0.5*pi), -1, 1, 280, 377).reciprocal, 0.9, 0.3) }.play